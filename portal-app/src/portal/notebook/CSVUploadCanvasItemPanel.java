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
import org.squonk.execution.steps.StepDefinitionConstants;
import portal.notebook.api.VariableInstance;
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
    private Form<ModelObject> form;
    private FileUploadField fileUploadField;
    @Inject
    private NotebookSession notebookSession;

    public CSVUploadCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        load();
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
        form.setOutputMarkupId(true);
        add(form);

    }

    private void processUpload(FileUpload upload) throws IOException {
        if (upload == null) {
            form.getModelObject().setErrorMessage("No file chosen");
        } else {
            String fileName = upload.getClientFileName();
            InputStream inputStream = upload.getInputStream();
            VariableInstance variableModel = notebookSession.getCurrentNotebookInstance().findVariable(getCellInstance().getId(), "fileContent");
            variableModel.setValue(fileName);
            notebookSession.storeCurrentNotebook();
            notebookSession.writeVariableFileContents(variableModel, inputStream);
            form.getModelObject().setFileName(upload.getClientFileName());
        }
    }

    private void execute() throws IOException {
        form.getModelObject().store();

        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellInstance().getId());
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
        try {
            execute();
            getRequestCycle().find(AjaxRequestTarget.class).add(CSVUploadCanvasItemPanel.this.form);
        } catch (Throwable t) {
            logger.error(null, t);
        }
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
            VariableInstance variableModel = notebookSession.getCurrentNotebookInstance().findVariable(getCellInstance().getId(), "fileContent");
            fileName = (String) variableModel.getValue();
            csvFormatType = (String) getCellInstance().getOptionMap().get(StepDefinitionConstants.CsvUpload.OPTION_NAME_FILE_TYPE).getValue();
            firstLineIsHeader = (Boolean) getCellInstance().getOptionMap().get(StepDefinitionConstants.CsvUpload.OPTION_NAME_FIRST_LINE_IS_HEADER).getValue();
        }

        public void store() {
            getCellInstance().getOptionMap().get(OPTION_FILE_TYPE).setValue(StepDefinitionConstants.CsvUpload.OPTION_NAME_FILE_TYPE);
            getCellInstance().getOptionMap().get(OPTION_FIRST_LINE_IS_HEADER).setValue(StepDefinitionConstants.CsvUpload.OPTION_NAME_FIRST_LINE_IS_HEADER);
        }

    }

}