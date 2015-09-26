package portal.webapp.notebook;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.CompoundPropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUploadCanvasItemPanel extends CanvasItemPanel<FileUploadCell> {
    @Inject
    private NotebooksSession notebooksSession;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadCanvasItemPanel.class.getName());

    private Form<UploadData> uploadForm;
    private FileUploadField fileUploadField;

    public FileUploadCanvasItemPanel(String id, Notebook notebook, FileUploadCell cell) {
        super(id, notebook, cell);
        addForm();
        getNotebook().registerVariablesForProducer(getCell());
        load();
    }

    private void addForm() {
        uploadForm = new Form<>("form");
        uploadForm.setOutputMarkupId(true);

        uploadForm.setModel(new CompoundPropertyModel<>(new UploadData()));
        TextField<String> fileNameField = new TextField<>("fileName");
        fileNameField.setEnabled(false);
        uploadForm.add(fileNameField);

        fileUploadField = new FileUploadField("fileInput");
        uploadForm.add(fileUploadField);

        uploadForm.add(new Image("appender", AbstractDefaultAjaxBehavior.INDICATOR));

        final AjaxSubmitLink submit = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    processUpload(fileUploadField.getFileUpload());
                    target.add(uploadForm);
                } catch (Throwable t) {
                    logger.error(null, t);
                }
            }
        };
        submit.setOutputMarkupId(true);
        uploadForm.add(submit);
        uploadForm.setOutputMarkupId(true);
        add(uploadForm);

    }

    private void processUpload(FileUpload upload) throws IOException {
        notebooksSession.uploadFile(upload.getClientFileName(), upload.getInputStream());
        uploadForm.getModelObject().setFileName(upload.getClientFileName());
        store();
    }

    private void load() {
        uploadForm.getModelObject().setFileName(getCell().getFileName());
    }

    private void store() {
        getNotebook().findVariable(getCell(), "resourceId").setValue(uploadForm.getModelObject().getFileName());
        getCell().setFileName(uploadForm.getModelObject().getFileName());
        notebooksSession.saveNotebook(getNotebook());
    }


    private class UploadData implements Serializable {

        private String fileName;
        private List<FileUpload> fileInput = new ArrayList<FileUpload>();

        public List<FileUpload> getFileInput() {
            return fileInput;
        }

        public void setFileInput(List<FileUpload> fileInput) {
            this.fileInput = fileInput;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }

}