package portal.notebook;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;

import javax.inject.Inject;
import java.io.Serializable;

public class GroovyScriptTrustedCanvasItemPanel extends CanvasItemPanel {

    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public GroovyScriptTrustedCanvasItemPanel(String id, CellModel cell) {
        super(id, cell);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        load();
    }

    private void load() {
        form.getModelObject().load();
    }

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        form.setOutputMarkupId(true);
        TextArea<String> scriptTextArea = new TextArea<String>("script");
        form.add(scriptTextArea);
        add(form);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        form.getModelObject().store();
        VariableModel outputVariableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getName(), "output");
        outputVariableModel.setValue(null);
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellModel().getName());
        fireContentChanged();
    }

    class ModelObject implements Serializable {

        private String script;

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public void load() {
            script = (String) getCellModel().getOptionModelMap().get("script").getValue();
        }

        public void store() {
            getCellModel().getOptionModelMap().get("script").setValue(script);
        }
    }

}
