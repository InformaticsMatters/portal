package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;

public class DatasetMergerCanvasItemPanel extends CanvasItemPanel {

    private final CellTitleBarPanel.CallbackHandler callbackHandler;
    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public DatasetMergerCanvasItemPanel(String id, CellModel cell, CellTitleBarPanel.CallbackHandler callbackHandler) {
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
        TextField<String> mergeFieldName = new TextField<String>("mergeFieldName");
        form.add(mergeFieldName);
        CheckBox keepFirstField = new CheckBox("keepFirst");
        form.add(keepFirstField);
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

        private String mergeFieldName;
        private Boolean keepFirst;

        public Boolean getKeepFirst() {
            return keepFirst;
        }

        public void setKeepFirst(Boolean keepFirst) {
            this.keepFirst = keepFirst;
        }

        public String getMergeFieldName() {
            return mergeFieldName;
        }

        public void setMergeFieldName(String mergeFieldName) {
            this.mergeFieldName = mergeFieldName;
        }

        public void load() {
            keepFirst = (Boolean) getCellModel().getOptionModelMap().get("keepFirst").getValue();
            mergeFieldName = (String) getCellModel().getOptionModelMap().get("mergeFieldName").getValue();
        }

        public void store() {
            getCellModel().getOptionModelMap().get("keepFirst").setValue(keepFirst);
            getCellModel().getOptionModelMap().get("mergeFieldName").setValue(mergeFieldName);
        }
    }

}
