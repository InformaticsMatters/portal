package portal.notebook;

import com.im.lac.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import portal.dataset.IDatasetDescriptor;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class TableDisplayCanvasItemPanel extends CanvasItemPanel<TableDisplayCellModel> {
    @Inject
    private NotebookSession notebookSession;
    private Form<ModelObject> form;
    private TableDisplayVisualizer tableDisplayVisualizer;

    public TableDisplayCanvasItemPanel(String id, TableDisplayCellModel cell) {
        super(id, cell);
        addHeader();
        addInput();
        addGrid();
        addListeners();
        load();
    }

    private void addHeader() {
        add(new Label("cellName", getCellModel().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                notebookSession.getNotebookModel().removeCell(getCellModel());
                notebookSession.storeNotebook();
            }
        });
    }

    private void addInput() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        IModel<List<VariableModel>> dropDownModel = new IModel<List<VariableModel>>() {
            @Override
            public List<VariableModel> getObject() {
                List<VariableModel> list = notebookSession.listAvailableInputVariablesFor(getCellModel(), notebookSession.getNotebookModel());
                return list;
            }

            @Override
            public void setObject(List<VariableModel> variableList) {

            }

            @Override
            public void detach() {

            }
        };
        DropDownChoice<VariableModel> inputVariableChoice = new DropDownChoice<VariableModel>("inputVariable", dropDownModel);
        form.add(inputVariableChoice);
        IndicatingAjaxSubmitLink calculateLink = new IndicatingAjaxSubmitLink("display") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                displayAndSave();
            }
        };
        form.add(calculateLink);
        add(form);
    }

    private void addGrid() {
        addOrReplaceTreeGridVisualizer(new TableDisplayDescriptor(0l, "", 0));
    }

    private void addListeners() {
        VariableChangeListener variableChangeListener = new VariableChangeListener() {

            @Override
            public void onValueChanged(VariableModel source, Object oldValue) {
                refresh();
            }

            @Override
            public void onVariableRemoved(VariableModel source) {
                refresh();
            }
        };
        for (VariableModel variableModel : notebookSession.getNotebookModel().getVariableModelList()) {
            variableModel.removeChangeListener(variableChangeListener);
            variableModel.addChangeListener(variableChangeListener);
        }
        notebookSession.getNotebookModel().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(CellModel cellModel) {
                refresh();

            }

            @Override
            public void onCellAdded(CellModel cellModel) {
                refresh();
            }
        });
    }

    private void displayAndSave() {
        getCellModel().setInputVariableModel(form.getModelObject().getInputVariable());
        loadTableData();
        notebookSession.storeNotebook();
    }

    private void refresh() {
        getRequestCycle().find(AjaxRequestTarget.class).add(form);
        loadTableData();
    }

    private void load() {
        form.getModelObject().setInputVariable(getCellModel().getInputVariableModel());
        loadTableData();
    }

    private void loadTableData() {
        boolean assigned = getCellModel().getInputVariableModel() != null && getCellModel().getInputVariableModel().getValue() != null;
        IDatasetDescriptor descriptor = assigned ? loadDescriptor() : null;
        if (descriptor == null) {
            descriptor = new TableDisplayDescriptor(0l, "", 0);
        }
        addOrReplaceTreeGridVisualizer(descriptor);
    }

    private IDatasetDescriptor loadDescriptor() {
        if (getCellModel().getInputVariableModel().getValue() instanceof String) {
            return notebookSession.loadDatasetFromFile(getCellModel().getInputVariableModel().getValue().toString());
        } else if (getCellModel().getInputVariableModel().getValue() instanceof Strings) {
            return notebookSession.createDatasetFromStrings((Strings) getCellModel().getInputVariableModel().getValue(), getCellModel().getInputVariableModel().getName());
        } else if (getCellModel().getInputVariableModel().getValue() instanceof List) {
            return notebookSession.createDatasetFromMolecules((List<MoleculeObject>) getCellModel().getInputVariableModel().getValue(), getCellModel().getInputVariableModel().getName());
        } else {
            return null;
        }
    }

    private void addOrReplaceTreeGridVisualizer(IDatasetDescriptor datasetDescriptor) {
        tableDisplayVisualizer = new TableDisplayVisualizer("visualizer", datasetDescriptor);
        addOrReplace(tableDisplayVisualizer);
        TableDisplayNavigationPanel treeGridNavigation = new TableDisplayNavigationPanel("navigation", tableDisplayVisualizer);
        addOrReplace(treeGridNavigation);
    }

    class ModelObject implements Serializable {
        private VariableModel inputVariable;

        public VariableModel getInputVariable() {
            return inputVariable;
        }

        public void setInputVariable(VariableModel inputVariable) {
            this.inputVariable = inputVariable;
        }
    }

}
