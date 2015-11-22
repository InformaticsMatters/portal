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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileUploadCanvasItemPanel extends CanvasItemPanel<FileUploadCellModel> {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<UploadData> uploadForm;
    private FileUploadField fileUploadField;

    public FileUploadCanvasItemPanel(String id, FileUploadCellModel cell) {
        super(id, cell);
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
                ajaxRequestTarget.add(getParent());
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
        VariableModel variableModel = notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "file");
        variableModel.setValue(uploadForm.getModelObject().getFileName());
        getCellModel().setFileName(uploadForm.getModelObject().getFileName());
        notebookSession.storeNotebook();
        notebookSession.writeVariableFileContents(variableModel, upload.getClientFileName(), upload.getInputStream());
        notebookSession.reloadNotebook();
        uploadForm.getModelObject().setFileName(upload.getClientFileName());

    }

    private void load() {
        uploadForm.getModelObject().setFileName(getCellModel().getFileName());
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