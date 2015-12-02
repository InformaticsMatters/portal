package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
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
    private Form<UploadData> form;
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
        form = new Form<>("form");
        form.setOutputMarkupId(true);

        form.setModel(new CompoundPropertyModel<>(new UploadData()));
        Label fileNameLabel = new Label("fileName");
        form.add(fileNameLabel);

        fileUploadField = new FileUploadField("fileInput");
        form.add(fileUploadField);

        TextField<String> nameFieldNameField = new TextField<String>("nameFieldName");
        form.add(nameFieldNameField);

        IndicatingAjaxSubmitLink uploadLink = new IndicatingAjaxSubmitLink("upload", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    processUpload(fileUploadField.getFileUpload());
                    target.add(SDFUploadCanvasItemPanel.this.form);
                } catch (Throwable t) {
                    logger.error(null, t);
                }
            }
        };
        form.add(uploadLink);

        IndicatingAjaxSubmitLink executeLink = new IndicatingAjaxSubmitLink("execute", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    execute();
                    target.add(SDFUploadCanvasItemPanel.this.form);
                } catch (Throwable t) {
                    logger.error(null, t);
                }
            }
        };
        executeLink.setOutputMarkupId(true);
        add(executeLink);
        form.setOutputMarkupId(true);
        add(form);

    }

    private void processUpload(FileUpload upload) throws IOException {
        if (upload == null) {
            form.getModelObject().setErrorMessage("No file chosen");
        } else {
            String fileName = upload.getClientFileName();
            InputStream inputStream = upload.getInputStream();
            VariableModel variableModel = notebookSession.getNotebookModel().findVariableModel(getCellModel().getName(), "FileContent");
            variableModel.setValue(fileName);
            getCellModel().setNameFieldName(form.getModelObject().getNameFieldName());
            notebookSession.storeNotebook();
            notebookSession.writeVariableFileContents(variableModel, inputStream);
            form.getModelObject().setFileName(upload.getClientFileName());
        }

    }

    private void execute() throws IOException {
        System.out.println("SDFUploadCanvasItemPanel.execute() " + form.getModelObject().getNameFieldName());
        getCellModel().setNameFieldName(form.getModelObject().getNameFieldName());
        notebookSession.storeNotebook();
        notebookSession.executeCell(getCellModel().getName());
        notebookSession.reloadNotebook();
        form.getModelObject().setNameFieldName(getCellModel().getNameFieldName());
    }

    private void load() {
        VariableModel variableModel = notebookSession.getNotebookModel().findVariableModel(getCellModel().getName(), "FileContent");
        form.getModelObject().setFileName((String) variableModel.getValue());
        form.getModelObject().setNameFieldName(getCellModel().getNameFieldName());
    }


    private class UploadData implements Serializable {

        private String fileName;
        private List<FileUpload> fileInput = new ArrayList<FileUpload>();
        private String nameFieldName;
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

        public String getNameFieldName() {
            System.out.println("UploadData.getNameFieldName() -> " + nameFieldName);
            return nameFieldName;
        }

        public void setNameFieldName(String nameFieldName) {
            System.out.println("UploadData.setNameFieldName() -> " + nameFieldName);
            this.nameFieldName = nameFieldName;
        }
    }

}