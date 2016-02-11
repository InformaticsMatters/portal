package portal.notebook;

import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

import javax.inject.Inject;
import java.io.Serializable;

public class TransformValuesCanvasItemPanel extends CanvasItemPanel {

    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public TransformValuesCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = getCellInstance();
        if (cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(300);
        }
        if (cellInstance.getSizeHeight() == 0) {
            cellInstance.setSizeHeight(200);
        }
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        load();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript("fitDefinitionsArea('" + getMarkupId() + "')"));
        makeCanvasItemResizable(container, "fitDefinitionsArea", 300, 200);
    }

    private void load() {
        form.getModelObject().load();
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        form.setOutputMarkupId(true);
        TextArea<String> textArea = new TextArea<String>("transformDefinitions");
        form.add(textArea);
        add(form);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        form.getModelObject().store();
        VariableInstance outputVariableInstance = notebookSession.getCurrentNotebookInstance().findVariable(getCellInstance().getName(), "output");
        outputVariableInstance.setValue(null);
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellInstance().getId());
        fireContentChanged();
    }

    class ModelObject implements Serializable {

        private String transformDefinitions;

        public String getTransformDefinitions() {
            return transformDefinitions;
        }

        public void setTransformDefinitions(String transformDefinitions) {
            this.transformDefinitions = transformDefinitions;
        }

        public void load() {
            transformDefinitions = (String) getCellInstance().getOptionMap().get("transformDefinitions").getValue();
        }

        public void store() {
            getCellInstance().getOptionMap().get("transformDefinitions").setValue(transformDefinitions);
        }
    }

}
