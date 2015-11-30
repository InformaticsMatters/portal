package portal.notebook;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.CompoundPropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SDFUploadCanvasItemPanel extends CanvasItemPanel<SDFUploadCellModel> {
    private static final Logger logger = LoggerFactory.getLogger(SDFUploadCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<UploadData> uploadForm;
    private FileUploadField fileUploadField;

    public SDFUploadCanvasItemPanel(String id, SDFUploadCellModel cell) {
        super(id, cell);
        setOutputMarkupId(true);
        addHeader();
        addForm();
        load();
    }

    private void addHeader() {
        add(new Label("cellName", getCellModel().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                notebookSession.getNotebookModel().removeCell(getCellModel());
                notebookSession.storeNotebook();
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

        IndicatingAjaxSubmitLink submit = new IndicatingAjaxSubmitLink("submit", uploadForm) {

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
        add(submit);
        uploadForm.setOutputMarkupId(true);
        add(uploadForm);

    }

    private void processUpload(FileUpload upload) throws IOException {
        if (upload == null) {
            uploadForm.getModelObject().setErrorMessage("No file chosen");
        } else {
            String fileName = upload.getClientFileName();
            InputStream inputStream = upload.getInputStream();
            VariableModel variableModel = notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "file");
            variableModel.setValue(fileName);
            getCellModel().setFileName(fileName);
            notebookSession.storeNotebook();
            notebookSession.writeVariableFileContents(variableModel, inputStream);
            notebookSession.reloadNotebook();
            uploadForm.getModelObject().setFileName(upload.getClientFileName());
        }

    }

    private void load() {
        uploadForm.getModelObject().setFileName(getCellModel().getFileName());
    }



    private class UploadData implements Serializable {

        private String fileName;
        private List<FileUpload> fileInput = new ArrayList<FileUpload>();
        private String errorMessage;

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

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

}