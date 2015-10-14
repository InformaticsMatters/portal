package portal.webapp.notebook;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
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
    private static final Logger logger = LoggerFactory.getLogger(FileUploadCanvasItemPanel.class.getName());
    @Inject
    private NotebooksSession notebooksSession;
    private Form<UploadData> uploadForm;
    private FileUploadField fileUploadField;

    public FileUploadCanvasItemPanel(String id, Notebook notebook, FileUploadCell cell) {
        super(id, notebook, cell);
        addHeader();
        addForm();
        load();
    }

    private void addHeader() {
        add(new Label("cellName", getCell().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getNotebook().removeCell(getCell());
                notebooksSession.saveNotebook(getNotebook());
            }
        });
    }


    private void addForm() {
        uploadForm = new Form<>("form");
        uploadForm.setOutputMarkupId(true);

        uploadForm.setModel(new CompoundPropertyModel<>(new UploadData()));
        Label fileNameField = new Label("fileName");
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
        uploadFile(upload.getClientFileName(), upload.getInputStream());
        uploadForm.getModelObject().setFileName(upload.getClientFileName());
        store();
    }

    private void uploadFile(String clientFileName, InputStream inputStream) {
        try {
            OutputStream outputStream = new FileOutputStream("files/" + clientFileName);
            try {
                byte[] buffer = new byte[4096];
                int r = inputStream.read(buffer, 0, buffer.length);
                while (r > -1) {
                    outputStream.write(buffer, 0, r);
                    r = inputStream.read(buffer);
                }
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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