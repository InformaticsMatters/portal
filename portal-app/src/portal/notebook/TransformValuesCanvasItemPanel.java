package portal.notebook;

import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.CompoundPropertyModel;

import javax.inject.Inject;
import java.io.Serializable;

public class TransformValuesCanvasItemPanel extends CanvasItemPanel {

    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public TransformValuesCanvasItemPanel(String id, CellModel cell) {
        super(id, cell);
        if (cell.getSizeWidth() == 0) {
            cell.setSizeWidth(300);
        }
        if (cell.getSizeHeight() == 0) {
            cell.setSizeHeight(200);
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
        VariableModel outputVariableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getId(), "output");
        outputVariableModel.setValue(null);
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellModel().getId());
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
            transformDefinitions = (String) getCellModel().getOptionModelMap().get("transformDefinitions").getValue();
        }

        public void store() {
            getCellModel().getOptionModelMap().get("transformDefinitions").setValue(transformDefinitions);
        }
    }

}
