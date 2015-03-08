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
import portal.service.api.DatasetService;
import portal.service.api.DatasetStreamFormat;
import portal.service.api.ImportFromStreamData;
import toolkit.wicket.semantic.SemanticModalPanel;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UploadModalPanel extends SemanticModalPanel {

    @Inject
    private DatasetService service;
    private Callbacks callbacks;
    private Form<UploadModalData> form;
    private FileUploadField fileUploadField;

    public UploadModalPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    private void addForm() {
        form = new Form<>("form");
        form.setOutputMarkupId(true);
        getModalRootComponent().add(form);

        form.setModel(new CompoundPropertyModel<>(new UploadModalData()));
        TextField<String> descriptionField = new TextField<>("description");
        form.add(descriptionField);

        fileUploadField = new FileUploadField("fileInput");
        form.add(fileUploadField);

        form.add(new Image("appender", AbstractDefaultAjaxBehavior.INDICATOR));

        final AjaxSubmitLink submit = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                processSubmission();
                callbacks.onSubmit();
                hideModal();
            }
        };
        submit.setOutputMarkupId(true);
        form.add(submit);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onCancel();
            }
        };
        form.add(cancelAction);
    }

    private void processSubmission() {
        try {
            for (FileUpload upload : fileUploadField.getFileUploads()) {
                ImportFromStreamData data = new ImportFromStreamData();
                data.setDescription(form.getModelObject().getDescription());
                data.setDatasetStreamFormat(DatasetStreamFormat.SDF);
                data.setInputStream(upload.getInputStream());
                data.setFieldConfigMap(new HashMap<>());
                service.importFromStream(data);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

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
