package portal.notebook;

import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.notebook.api.VariableInstance;

import javax.inject.Inject;
import java.io.Serializable;

public class GroovyScriptTrustedCanvasItemPanel extends CanvasItemPanel {

    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public GroovyScriptTrustedCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        getCellInstance().setSizeWidth(300);
        getCellInstance().setSizeHeight(250);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        load();
    }

    private void load() {
        form.getModelObject().load();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript("makeCanvasItemResizable('" + getMarkupId() + "')"));
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
        VariableInstance outputVariableInstance = notebookSession.getCurrentNotebookInstance().findVariable(getCellInstance().getId(), "output");
        outputVariableInstance.setValue(null);
        notebookSession.executeCell(getCellInstance().getId());
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
            script = (String) getCellInstance().getOptionMap().get("script").getValue();
        }

        public void store() {
            getCellInstance().getOptionMap().get("script").setValue(script);
        }
    }

}
