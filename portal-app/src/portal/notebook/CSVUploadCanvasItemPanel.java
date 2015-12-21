package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
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
import java.util.Arrays;
import java.util.List;

public class CSVUploadCanvasItemPanel extends CanvasItemPanel {

    private static final Logger logger = LoggerFactory.getLogger(CSVUploadCanvasItemPanel.class.getName());
    private static final List<String> CSV_FORMATS = Arrays.asList("DEFAULT", "RFC4180", "EXCEL", "MYSQL", "TDF");
    private final CellCallbackHandler callbackHandler;
    private Form<ModelObject> form;
    private FileUploadField fileUploadField;
    @Inject
    private NotebookSession notebookSession;

    public CSVUploadCanvasItemPanel(String id, CellModel cell, CellCallbackHandler callbackHandler) {
        super(id, cell);
        setOutputMarkupId(true);
        addForm();
        load();
        this.callbackHandler = callbackHandler;
    }

    private void addForm() {
        form = new Form<>("form");
        form.setOutputMarkupId(true);

        form.setModel(new CompoundPropertyModel<>(new ModelObject()));
        Label fileNameLabel = new Label("fileName");
        form.add(fileNameLabel);

        fileUploadField = new FileUploadField("fileInput");
        form.add(fileUploadField);

        DropDownChoice<String> csvFormatChoice = new DropDownChoice<String>("csvFormatType", CSV_FORMATS);
        form.add(csvFormatChoice);

        CheckBox firstLineIsHeaderField = new CheckBox("firstLineIsHeader");
        form.add(firstLineIsHeaderField);

        IndicatingAjaxSubmitLink uploadLink = new IndicatingAjaxSubmitLink("upload", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    processUpload(fileUploadField.getFileUpload());
                    target.add(CSVUploadCanvasItemPanel.this.form);
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
                    target.add(CSVUploadCanvasItemPanel.this.form);
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
            VariableModel variableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getName(), "fileContent");
            variableModel.setValue(fileName);
            notebookSession.storeCurrentNotebook();
            notebookSession.writeVariableFileContents(variableModel, inputStream);
            form.getModelObject().setFileName(upload.getClientFileName());
        }
    }

    private void execute() throws IOException {
        //System.out.println("File type is " + form.getModelObject().getCsvFormatType());
        //System.out.println("First line header " + form.getModelObject().isFirstLineIsHeader());
        form.getModelObject().store();

        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellModel().getName());
        fireContentChanged();

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

    }

    private class ModelObject implements Serializable {
        public static final String OPTION_FILE_TYPE = "csvFormatType";
        public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";

        private String fileName;
        private List<FileUpload> fileInput = new ArrayList<FileUpload>();
        private String csvFormatType;
        private Boolean firstLineIsHeader;
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

        public String getCsvFormatType() {
            return csvFormatType;
        }

        public void setCsvFormatType(String csvFormatType) {
            this.csvFormatType = csvFormatType;
        }

        public Boolean isFirstLineIsHeader() {
            return firstLineIsHeader;
        }

        public void setFirstLineIsHeader(Boolean firstLineIsHeader) {
            this.firstLineIsHeader = firstLineIsHeader;
        }

        public void load() {
            VariableModel variableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getName(), "fileContent");
            fileName = (String) variableModel.getValue();
            csvFormatType = (String) getCellModel().getOptionModelMap().get(OPTION_FILE_TYPE).getValue();
            firstLineIsHeader = (Boolean) getCellModel().getOptionModelMap().get(OPTION_FIRST_LINE_IS_HEADER).getValue();
        }

        public void store() {
            getCellModel().getOptionModelMap().get(OPTION_FILE_TYPE).setValue(csvFormatType);
            getCellModel().getOptionModelMap().get(OPTION_FIRST_LINE_IS_HEADER).setValue(firstLineIsHeader);
        }

    }

}