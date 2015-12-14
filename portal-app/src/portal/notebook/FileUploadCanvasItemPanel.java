package portal.notebook;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
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

public class FileUploadCanvasItemPanel extends CanvasItemPanel {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<UploadData> form;
    private FileUploadField fileUploadField;

    public FileUploadCanvasItemPanel(String id, CellModel cell, CallbackHandler callbackHandler) {
        super(id, cell, callbackHandler);
        setOutputMarkupId(true);
        addForm();
        load();
    }

    private void addForm() {
        form = new Form<>("form");
        form.setOutputMarkupId(true);

        form.setModel(new CompoundPropertyModel<>(new UploadData()));
        Label fileNameField = new Label("fileName");
        form.add(fileNameField);

        fileUploadField = new FileUploadField("fileInput");
        form.add(fileUploadField);

        form.add(new Image("appender", AbstractDefaultAjaxBehavior.INDICATOR));

        IndicatingAjaxSubmitLink submit = new IndicatingAjaxSubmitLink("submit", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    processUpload(fileUploadField.getFileUpload());
                    target.add(FileUploadCanvasItemPanel.this.form);
                } catch (Throwable t) {
                    logger.error(null, t);
                }
            }
        };
        submit.setOutputMarkupId(true);
        add(submit);
        form.setOutputMarkupId(true);
        add(form);

    }

    private void processUpload(FileUpload upload) throws IOException {
        if (upload == null) {
            form.getModelObject().setErrorMessage("No file chosen");
        } else {
            String fileName = upload.getClientFileName();
            InputStream inputStream = upload.getInputStream();
            VariableModel variableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getName(), "file");
            variableModel.setValue(fileName);
            form.getModelObject().store();
            getCellModel().getOptionModelMap().get("fileName").setValue(fileName);
            notebookSession.storeCurrentNotebook();
            notebookSession.writeVariableFileContents(variableModel, inputStream);
            notebookSession.reloadCurrentNotebook();
            form.getModelObject().setFileName(upload.getClientFileName());
        }

    }

    private void load() {
        form.getModelObject().load();
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

        public void store() {
            getCellModel().getOptionModelMap().get("fileName").setValue(fileName);
        }

        public void load() {
            fileName = (String) getCellModel().getOptionModelMap().get("fileName").getValue();
        }
    }

}