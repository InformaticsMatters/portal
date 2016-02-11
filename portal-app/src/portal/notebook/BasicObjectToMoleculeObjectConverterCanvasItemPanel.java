package portal.notebook;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.notebook.api.VariableInstance;

import javax.inject.Inject;
import java.io.Serializable;

public class BasicObjectToMoleculeObjectConverterCanvasItemPanel extends CanvasItemPanel {

    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public BasicObjectToMoleculeObjectConverterCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        load();
    }

    private void load() {
        form.getModelObject().load();
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        form.setOutputMarkupId(true);
        TextField<String> structureFieldName = new TextField<String>("structureFieldName");
        form.add(structureFieldName);
        add(form);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        form.getModelObject().store();
        VariableInstance outputVariableInstance = notebookSession.getCurrentNotebookInstance().findVariable(getCellInstance().getId(), "output");
        outputVariableInstance.setValue(null);
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellInstance().getId());
        fireContentChanged();
    }

    class ModelObject implements Serializable {

        private String structureFieldName;

        public String getStructureFieldName() {
            return structureFieldName;
        }

        public void setStructureFieldName(String structureFieldName) {
            this.structureFieldName = structureFieldName;
        }

        public void load() {
            structureFieldName = (String) getCellInstance().getOptionMap().get("structureFieldName").getValue();
        }

        public void store() {
            getCellInstance().getOptionMap().get("structureFieldName").setValue(structureFieldName);
        }
    }

}
