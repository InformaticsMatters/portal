package portal.webapp;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.CompoundPropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toolkit.wicket.semantic.SemanticModalPanel;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UploadModalPanel extends SemanticModalPanel {

    private static final Logger logger = LoggerFactory.getLogger(UploadModalPanel.class.getName());

    private Callbacks callbacks;
    private Form<UploadModalData> uploadForm;
    private FileUploadField fileUploadField;

    public UploadModalPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    private void addForm() {
        uploadForm = new Form<>("form");
        uploadForm.setOutputMarkupId(true);
        getModalRootComponent().add(uploadForm);

        uploadForm.setModel(new CompoundPropertyModel<>(new UploadModalData()));
        TextField<String> descriptionField = new TextField<>("description");
        uploadForm.add(descriptionField);

        fileUploadField = new FileUploadField("fileInput");
        uploadForm.add(fileUploadField);

        uploadForm.add(new Image("appender", AbstractDefaultAjaxBehavior.INDICATOR));

        final AjaxSubmitLink submit = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    for (FileUpload upload : fileUploadField.getFileUploads()) {
                        callbacks.onSubmit(uploadForm.getModelObject().getDescription(), upload.getInputStream());
                    }
                    hideModal();
                } catch (Throwable t) {
                    logger.error(null, t);
                }
            }
        };
        submit.setOutputMarkupId(true);
        uploadForm.add(submit);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onCancel();
            }
        };
        uploadForm.add(cancelAction);
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public interface Callbacks extends Serializable {

        void onSubmit(String name, InputStream inputStream);

        void onCancel();

    }

    private class UploadModalData implements Serializable {

        private String description;
        private List<FileUpload> fileInput = new ArrayList<FileUpload>();

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<FileUpload> getFileInput() {
            return fileInput;
        }

        public void setFileInput(List<FileUpload> fileInput) {
            this.fileInput = fileInput;
        }
    }
}
