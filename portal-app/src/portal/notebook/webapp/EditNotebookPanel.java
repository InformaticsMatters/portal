package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.SessionContext;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticModalPanel;

import javax.inject.Inject;
import java.io.Serializable;

public class EditNotebookPanel extends SemanticModalPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditNotebookPanel.class);
    private Callbacks callbacks;
    private Form<EditNotebookData> form;
    private AjaxSubmitLink submitAction;
    private TextField<String> nameField;
    private TextField<String> descriptionField;
    private boolean forRemove;
    private Long notebookId;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private SessionContext sessionContext;
    @Inject
    private NotifierProvider notifierProvider;

    public EditNotebookPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    private void addForm() {

        Label titleLabel = new Label("title", new IModel<String>() {
            @Override
            public void detach() {

            }

            @Override
            public String getObject() {
                if (notebookId == null) {
                    return "New notebook";
                } else if (forRemove) {
                    return "Remove notebook";
                } else {
                    return "Edit notebook";
                }
            }

            @Override
            public void setObject(String s) {

            }
        });
        getModalRootComponent().add(titleLabel);

        form = new Form<>("form");
        form.setOutputMarkupId(true);
        getModalRootComponent().add(form);
        form.setModel(new CompoundPropertyModel<>(new EditNotebookData()));

        nameField = new TextField<>("name");
        nameField.setRequired(true);
        form.add(nameField);

        descriptionField = new TextField<>("description");
        form.add(descriptionField);

        submitAction = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    Long id = store();
                    callbacks.onSubmit(id);
                } catch (Throwable t) {
                    LOGGER.warn("Error storing notebook", t);
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
            }
        };
        form.add(cancelAction);
    }

    private Long store() throws Exception {
        EditNotebookData editNotebookData = form.getModelObject();
        if (editNotebookData.getId() == null) {
            return notebookSession.createNotebook(editNotebookData.getName(), editNotebookData.getDescription(), editNotebookData.getShared());
        } else if (forRemove) {
            notebookSession.removeNotebook(editNotebookData.getId());
            return null;
        } else {
            notebookSession.updateNotebook(editNotebookData.getId(), editNotebookData.getName(), editNotebookData.getDescription(), editNotebookData.getShared());
            return editNotebookData.getId();
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void configureForCreate() {
        this.notebookId = null;
        EditNotebookData data = new EditNotebookData();
        data.setShared(false);
        form.setModelObject(data);
        nameField.setEnabled(true);
        descriptionField.setEnabled(true);
        forRemove = false;
    }

    public void configureForEdit(Long id) throws Exception {
        this.notebookId = id;
        NotebookInfo notebookInfo = notebookSession.findNotebookInfo(id);
        EditNotebookData editNotebookData = new EditNotebookData();
        editNotebookData.setId(notebookInfo.getId());
        editNotebookData.setName(notebookInfo.getName());
        editNotebookData.setDescription(notebookInfo.getDescription());
        editNotebookData.setShared(notebookInfo.getShared());
        form.setModelObject(editNotebookData);
        nameField.setEnabled(true);
        descriptionField.setEnabled(true);
        forRemove = false;
    }

    public void configureForRemove(Long id) throws Exception {
        this.notebookId = id;
        NotebookInfo notebookInfo = notebookSession.findNotebookInfo(id);
        EditNotebookData editNotebookData = new EditNotebookData();
        editNotebookData.setId(notebookInfo.getId());
        editNotebookData.setName(notebookInfo.getName());
        editNotebookData.setDescription(notebookInfo.getDescription());
        editNotebookData.setShared(notebookInfo.getShared());
        form.setModelObject(editNotebookData);
        nameField.setEnabled(false);
        descriptionField.setEnabled(false);
        forRemove = true;
    }

    public interface Callbacks extends Serializable {

        void onSubmit(Long id);

        void onCancel();

    }


    private class EditNotebookData implements Serializable {
        private Long id;
        private String name;
        private String description;
        private Boolean shared;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getShared() {
            return shared;
        }

        public void setShared(Boolean shared) {
            this.shared = shared;
        }
    }
}
