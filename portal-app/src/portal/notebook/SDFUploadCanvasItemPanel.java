package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
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

public class SDFUploadCanvasItemPanel extends CanvasItemPanel {

    private static final Logger logger = LoggerFactory.getLogger(SDFUploadCanvasItemPanel.class.getName());
    private Form<UploadData> form;
    private FileUploadField fileUploadField;
    @Inject
    private NotebookSession notebookSession;

    public SDFUploadCanvasItemPanel(String id, CellModel cell) {
        super(id, cell);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        load();
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
        form.setOutputMarkupId(true);
        add(form);

    }

    private void processUpload(FileUpload upload) throws IOException {
        if (upload == null) {
            form.getModelObject().setErrorMessage("No file chosen");
        } else {
            String fileName = upload.getClientFileName();
            InputStream inputStream = upload.getInputStream();
            VariableModel variableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getId(), "fileContent");
            variableModel.setValue(fileName);
            form.getModelObject().store();
            notebookSession.storeCurrentNotebook();
            notebookSession.writeVariableFileContents(variableModel, inputStream);
            form.getModelObject().setFileName(upload.getClientFileName());
        }

    }

    private void execute() throws IOException {
        form.getModelObject().store();
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellModel().getId());
        notebookSession.reloadCurrentNotebook();
    }

    private void load() {
        form.getModelObject().load();
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        try {
            execute();
            getRequestCycle().find(AjaxRequestTarget.class).add(SDFUploadCanvasItemPanel.this.form);
        } catch (Throwable t) {
            logger.error(null, t);
        }
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
            return nameFieldName;
        }

        public void setNameFieldName(String nameFieldName) {
            this.nameFieldName = nameFieldName;
        }

        public void store() {
            getCellModel().getOptionModelMap().get("nameFieldName").setValue(nameFieldName);
        }

        public void load() {
            VariableModel variableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getId(), "fileContent");
            fileName = variableModel == null ? null : (String) variableModel.getValue();
            nameFieldName = (String) getCellModel().getOptionModelMap().get("nameFieldName").getValue();
        }
    }

}