package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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

public class CSVUploadCanvasItemPanel extends CanvasItemPanel<CSVUploadCellModel> {
    private static final Logger logger = LoggerFactory.getLogger(CSVUploadCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<UploadData> form;
    private FileUploadField fileUploadField;

    private static final List<String> CSV_FORMATS = Arrays.asList(new String[] {
        "DEFAULT", "RFC4180", "EXCEL", "MYSQL", "TDF"
    });

    public CSVUploadCanvasItemPanel(String id, CSVUploadCellModel cell) {
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
            VariableModel variableModel = notebookSession.getNotebookModel().findVariableModel(getCellModel().getName(), "FileContent");
            variableModel.setValue(fileName);
            notebookSession.storeNotebook();
            notebookSession.writeVariableFileContents(variableModel, inputStream);
            notebookSession.reloadNotebook();
            form.getModelObject().setFileName(upload.getClientFileName());
        }

    }

    private void execute() throws IOException {
        System.out.println("File type is " + form.getModelObject().getCsvFormatType());
        System.out.println("First line header " + form.getModelObject().isFirstLineIsHeader());
        getCellModel().setCsvFormatType(form.getModelObject().getCsvFormatType());
        getCellModel().setFirstLineIsHeader(form.getModelObject().isFirstLineIsHeader());

        System.out.println("FTYP set? " + getCellModel().getCsvFormatType());
        System.out.println("FLIH set? " + getCellModel().isFirstLineIsHeader());

        notebookSession.storeNotebook();
        notebookSession.executeCell(getCellModel().getName());
        notebookSession.reloadNotebook();

        System.out.println("FTYP set? " + getCellModel().getCsvFormatType());
        System.out.println("FLIH set? " + getCellModel().isFirstLineIsHeader());


        form.getModelObject().setCsvFormatType(getCellModel().getCsvFormatType());
        form.getModelObject().setFirstLineIsHeader(getCellModel().isFirstLineIsHeader());
    }

    private void load() {
        VariableModel variableModel = notebookSession.getNotebookModel().findVariableModel(getCellModel().getName(), "FileContent");
        form.getModelObject().setFileName((String) variableModel.getValue());
        form.getModelObject().setCsvFormatType(getCellModel().getCsvFormatType());
        form.getModelObject().setFirstLineIsHeader(getCellModel().isFirstLineIsHeader());
    }


    private class UploadData implements Serializable {

        private String fileName;
        private List<FileUpload> fileInput = new ArrayList<FileUpload>();
        private String csvFormatType;
        private boolean firstLineIsHeader;
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

        public boolean isFirstLineIsHeader() {
            return firstLineIsHeader;
        }

        public void setFirstLineIsHeader(boolean firstLineIsHeader) {
            this.firstLineIsHeader = firstLineIsHeader;
        }

    }

}