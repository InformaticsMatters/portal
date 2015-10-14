package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import portal.dataset.IDatasetDescriptor;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class TableDisplayCanvasItemPanel extends CanvasItemPanel<TableDisplayCell> {
    @Inject
    private NotebooksSession notebooksSession;
    private Form<ModelObject> form;
    private TableDisplayVisualizer tableDisplayVisualizer;

    public TableDisplayCanvasItemPanel(String id, Notebook notebook, TableDisplayCell cell) {
        super(id, notebook, cell);
        addHeader();
        addInput();
        addGrid();
        addListeners();
        load();
    }

    private void addHeader() {
        add(new Label("cellName", getCell().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getNotebook().removeCell(getCell());
                notebooksSession.saveNotebook(getNotebook());
            }
        });
    }

    private void addInput() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        IModel<List<Variable>> dropDownModel = new IModel<List<Variable>>() {
            @Override
            public List<Variable> getObject() {
                List<Variable> list = notebooksSession.listAvailableInputVariablesFor(getCell(), getNotebook());
                return list;
            }

            @Override
            public void setObject(List<Variable> variableList) {

            }

            @Override
            public void detach() {

            }
        };
        DropDownChoice<Variable> inputVariableChoice = new DropDownChoice<Variable>("inputVariable", dropDownModel);
        form.add(inputVariableChoice);
        AjaxSubmitLink calculateLink = new AjaxSubmitLink("display") {
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
            public void onValueChanged(Variable source, Object oldValue) {
                refresh();
            }

            @Override
            public void onVariableRemoved(Variable source) {
                refresh();
            }
        };
        for (Variable variable : getNotebook().getVariableList()) {
            variable.removeChangeListener(variableChangeListener);
            variable.addChangeListener(variableChangeListener);
        }
        getNotebook().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(Cell cell) {
                refresh();

            }

            @Override
            public void onCellAdded(Cell cell) {
                refresh();
            }
        });
    }

    private void displayAndSave() {
        getCell().setInputVariable(form.getModelObject().getInputVariable());
        loadTableData();
        notebooksSession.saveNotebook(getNotebook());
    }

    private void refresh() {
        getRequestCycle().find(AjaxRequestTarget.class).add(form);
        loadTableData();
    }

    private void load() {
        form.getModelObject().setInputVariable(getCell().getInputVariable());
    }

    private void loadTableData() {
        boolean assigned = getCell().getInputVariable() != null && getCell().getInputVariable().getValue() != null;
        IDatasetDescriptor descriptor = assigned ? notebooksSession.loadDatasetFromFile(getCell().getInputVariable().getValue().toString()) : new TableDisplayDescriptor(0l, "", 0);
        addOrReplaceTreeGridVisualizer(descriptor);
    }

    private void addOrReplaceTreeGridVisualizer(IDatasetDescriptor datasetDescriptor) {
        tableDisplayVisualizer = new TableDisplayVisualizer("visualizer", datasetDescriptor);
        addOrReplace(tableDisplayVisualizer);
        TableDisplayNavigationPanel treeGridNavigation = new TableDisplayNavigationPanel("navigation", tableDisplayVisualizer);
        addOrReplace(treeGridNavigation);
    }

    class ModelObject implements Serializable {
        private Variable inputVariable;

        public Variable getInputVariable() {
            return inputVariable;
        }

        public void setInputVariable(Variable inputVariable) {
            this.inputVariable = inputVariable;
        }
    }

}
