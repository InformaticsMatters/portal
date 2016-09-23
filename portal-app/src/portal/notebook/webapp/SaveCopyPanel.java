package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.SessionContext;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticModalPanel;

import javax.inject.Inject;
import java.io.Serializable;

public class SaveCopyPanel extends SemanticModalPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveCopyPanel.class);
    private String title;
    private Callbacks callbacks;
    private Form<SaveCopyData> form;
    private AjaxSubmitLink submitAction;
    private TextField<String> descriptionField;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private SessionContext sessionContext;
    @Inject
    private NotifierProvider notifierProvider;

    public SaveCopyPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private void addForm() {
        Label titleLabel = new Label("title", new PropertyModel(this, "title"));
        getModalRootComponent().add(titleLabel);

        form = new Form<>("form");
        form.setOutputMarkupId(true);
        getModalRootComponent().add(form);
        form.setModel(new CompoundPropertyModel<>(new SaveCopyData()));

        descriptionField = new TextField<>("description");
        descriptionField.setRequired(true);
        form.add(descriptionField);

        submitAction = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    callbacks.onSubmit();
                } catch (Throwable t) {
                    LOGGER.warn("Error creating version", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        };
        submitAction.setOutputMarkupId(true);
        form.add(submitAction);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onCancel();
                hideModal();
            }
        };
        form.add(cancelAction);
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public String getDescription() {
        return form.getModelObject().getDescription();
    }

    public void setDescription(String description) {
        form.getModelObject().setDescription(description);
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

        void onCancel();

    }

    private class SaveCopyData implements Serializable {

        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }
}
