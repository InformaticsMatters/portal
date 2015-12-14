package portal.notebook;

import com.im.lac.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import portal.dataset.IDatasetDescriptor;
import portal.notebook.service.Strings;
import tmp.squonk.notebook.api.VariableType;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class TableDisplayCanvasItemPanel extends CanvasItemPanel {
    @Inject
    private NotebookSession notebookSession;
    private Form<ModelObject> form;
    private TableDisplayVisualizer tableDisplayVisualizer;

    public TableDisplayCanvasItemPanel(String id, CellModel cell, CallbackHandler callbackHandler) {
        super(id, cell, callbackHandler);
        addInput();
        addGrid();
        addListeners();
        load();
        setOutputMarkupId(true);
    }

    private void addInput() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        IModel<List<VariableModel>> dropDownModel = new IModel<List<VariableModel>>() {
            @Override
            public List<VariableModel> getObject() {
                List<VariableModel> list = notebookSession.listAvailableInputVariablesFor(getCellModel(), getCellModel().getBindingModelMap().get("input"), notebookSession.getCurrentNotebookModel());
                return list;
            }

            @Override
            public void setObject(List<VariableModel> variableList) {

            }

            @Override
            public void detach() {

            }
        };
        DropDownChoice<VariableModel> inputVariableChoice = new DropDownChoice<VariableModel>("inputVariableModel", dropDownModel);
        form.add(inputVariableChoice);

        add(new IndicatingAjaxSubmitLink("display", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                displayAndSave();
            }
        });

        add(form);
    }

    private void addGrid() {
        addOrReplaceTreeGridVisualizer(new TableDisplayDatasetDescriptor(0l, "", 0));
    }

    private void addListeners() {
        VariableChangeListener variableChangeListener = new VariableChangeListener() {

            @Override
            public void onValueChanged(VariableModel source, Object oldValue) {
                refresh();
            }

            @Override
            public void onVariableRemoved(VariableModel source) {
                source.removeChangeListener(this);
                refresh();
            }
        };
        for (VariableModel variableModel : notebookSession.getCurrentNotebookModel().buildVariableModelList()) {
            variableModel.removeChangeListener(variableChangeListener);
            variableModel.addChangeListener(variableChangeListener);
        }
        notebookSession.getCurrentNotebookModel().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(CellModel cellModel) {
                if (cellModel != getCellModel()) {
                    refresh();
                }
            }

            @Override
            public void onCellAdded(CellModel cellModel) {
                refresh();
            }
        });
    }

    private void displayAndSave() {
        getCellModel().getBindingModelMap().get("input").setVariableModel(form.getModelObject().getInputVariableModel());
        loadTableData();
        notebookSession.storeCurrentNotebook();
    }

    private void refresh() {
        getRequestCycle().find(AjaxRequestTarget.class).add(form);
        loadTableData();
    }

    private void load() {
        BindingModel bindingModel = getCellModel().getBindingModelMap().get("input");
        VariableModel variableModel = bindingModel == null ? null : bindingModel.getVariableModel();
        form.getModelObject().setInputVariableModel(variableModel);
        loadTableData();
    }

    private void loadTableData() {
        BindingModel bindingModel = getCellModel().getBindingModelMap().get("input");
        VariableModel variableModel = bindingModel == null ? null : bindingModel.getVariableModel();
        boolean assigned = variableModel != null && variableModel.getValue() != null;
        IDatasetDescriptor descriptor = assigned ? loadDescriptor() : null;
        if (descriptor == null) {
            descriptor = new TableDisplayDatasetDescriptor(0l, "", 0);
        }
        addOrReplaceTreeGridVisualizer(descriptor);
    }

    private IDatasetDescriptor loadDescriptor() {
        CellModel cellModel = getCellModel();
        VariableModel variableModel = cellModel.getBindingModelMap().get("input").getVariableModel();
        if (variableModel.getVariableType().equals(VariableType.FILE)) {
            return notebookSession.loadDatasetFromFile(variableModel.getValue().toString());
        } else if (variableModel.getVariableType().equals(VariableType.DATASET)) {
            return notebookSession.loadDatasetFromSquonkDataset(variableModel);
        } else if (variableModel.getValue() instanceof Strings) {
            return notebookSession.createDatasetFromStrings((Strings) variableModel.getValue(), variableModel.getName());
        } else if (variableModel.getValue() instanceof List) {
            return notebookSession.createDatasetFromMolecules((List<MoleculeObject>) variableModel.getValue(), variableModel.getName());
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
        private VariableModel inputVariableModel;

        public VariableModel getInputVariableModel() {
            return inputVariableModel;
        }

        public void setInputVariableModel(VariableModel inputVariable) {
            this.inputVariableModel = inputVariable;
        }
    }

}
