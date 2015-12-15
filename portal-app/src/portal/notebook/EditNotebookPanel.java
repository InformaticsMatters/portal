package portal.notebook;

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
import portal.notebook.service.EditNotebookData;
import portal.notebook.service.NotebookInfo;
import toolkit.wicket.semantic.SemanticModalPanel;

import javax.inject.Inject;
import java.io.Serializable;

public class EditNotebookPanel extends SemanticModalPanel {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPanel.class);
    private Callbacks callbacks;
    private Form<EditNotebookData> form;
    private AjaxSubmitLink submitAction;
    @Inject
    private NotebookSession notebookSession;
    private TextField<String> nameField;
    private TextField<String> descriptionField;
    private boolean forRemove;
    private Long notebookId;

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

        nameField = new TextField<String>("name");
        form.add(nameField);

        descriptionField = new TextField<String>("description");
        form.add(descriptionField);

        submitAction = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                store();
                callbacks.onSubmit();
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

    private void store() {
        EditNotebookData editNotebookData = form.getModelObject();
        if (editNotebookData.getId() == null) {
            notebookSession.createNotebook(editNotebookData);
        } else if (forRemove) {
            notebookSession.removeNotebook(editNotebookData.getId());
        } else {
            notebookSession.updateNotebook(editNotebookData);
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void configureForCreate() {
        this.notebookId = null;
        form.setModelObject(new EditNotebookData());
        forRemove = false;
    }

    public void configureForEdit(Long id) {
        this.notebookId = id;
        NotebookInfo notebookInfo = notebookSession.retrieveNotebookInfo(id);
        EditNotebookData editNotebookData = new EditNotebookData();
        editNotebookData.setId(notebookInfo.getId());
        editNotebookData.setName(notebookInfo.getName());
        editNotebookData.setDescription(notebookInfo.getDescription());
        form.setModelObject(editNotebookData);
        forRemove = false;
    }

    public void configureForRemove(Long id) {
        this.notebookId = id;
        NotebookInfo notebookInfo = notebookSession.retrieveNotebookInfo(id);
        EditNotebookData editNotebookData = new EditNotebookData();
        editNotebookData.setId(notebookInfo.getId());
        editNotebookData.setName(notebookInfo.getName());
        editNotebookData.setDescription(notebookInfo.getDescription());
        form.setModelObject(editNotebookData);
        nameField.setEnabled(false);
        descriptionField.setEnabled(false);
        forRemove = true;
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

        void onCancel();

    }

    private class ModelObject implements Serializable {
    }
}
