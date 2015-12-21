package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;

public class ChemblActivitiesFetcherCanvasItemPanel extends CanvasItemPanel {

    private final CellTitleBarPanel.CallbackHandler callbackHandler;
    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public ChemblActivitiesFetcherCanvasItemPanel(String id, CellModel cell, CellTitleBarPanel.CallbackHandler callbackHandler) {
        super(id, cell);
        addForm();
        load();
        setOutputMarkupId(true);
        this.callbackHandler = callbackHandler;
    }

    private void load() {
        form.getModelObject().load();
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        form.setOutputMarkupId(true);
        TextField<String> assayIdField = new TextField<String>("assayId");
        form.add(assayIdField);
        TextField<String> prefixField = new TextField<String>("prefix");
        form.add(prefixField);
        IndicatingAjaxSubmitLink executeLink = new IndicatingAjaxSubmitLink("submit", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                execute();
            }
        };
        executeLink.setOutputMarkupId(true);
        add(executeLink);
        add(form);
    }

    private void execute() {
        form.getModelObject().store();
        VariableModel outputVariableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getName(), "results");
        outputVariableModel.setValue(null);
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellModel().getName());
        callbackHandler.onContentChanged();
    }

    class ModelObject implements Serializable {

        private String assayId;
        private String prefix;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getAssayId() {
            return assayId;
        }

        public void setAssayId(String assayId) {
            this.assayId = assayId;
        }

        public void load() {
            assayId = (String) getCellModel().getOptionModelMap().get("assayId").getValue();
            prefix = (String) getCellModel().getOptionModelMap().get("prefix").getValue();
        }

        public void store() {
            getCellModel().getOptionModelMap().get("assayId").setValue(assayId);
            getCellModel().getOptionModelMap().get("prefix").setValue(prefix);
        }

    }

}
