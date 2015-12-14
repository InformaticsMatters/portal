package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.logging.Logger;

public class DatasetMergerCanvasItemPanel extends CanvasItemPanel {
    private static final Logger LOGGER = Logger.getLogger(DatasetMergerCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<ModelObject> form;

    public DatasetMergerCanvasItemPanel(String id, CellModel cell, CallbackHandler callbackHandler) {
        super(id, cell, callbackHandler);
        addForm();
        addListeners();
        load();
        setOutputMarkupId(true);
    }

    private void addListeners() {
        notebookSession.getCurrentNotebookModel().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(CellModel cellModel) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(form);
            }

            @Override
            public void onCellAdded(CellModel cellModel) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(form);
            }
        });
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
        notebookSession.reloadCurrentNotebook();
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
