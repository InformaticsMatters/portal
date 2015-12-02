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

public class FileUploadCanvasItemPanel extends CanvasItemPanel {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<UploadData> form;
    private FileUploadField fileUploadField;

    public FileUploadCanvasItemPanel(String id, CellModel cell) {
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
            VariableModel variableModel = notebookSession.getNotebookModel().findVariableModel(getCellModel().getName(), "file");
            variableModel.setValue(fileName);
            form.getModelObject().store();
            getCellModel().getOptionMap().put("fileName", fileName);
            notebookSession.storeNotebook();
            notebookSession.writeVariableFileContents(variableModel, inputStream);
            notebookSession.reloadNotebook();
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
            getCellModel().getOptionMap().put("fileName", fileName);
        }

        public void load() {
            fileName = (String) getCellModel().getOptionMap().get("fileName");
        }
    }

}