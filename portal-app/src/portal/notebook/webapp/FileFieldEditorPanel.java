package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileFieldEditorPanel extends FieldEditorPanel {
    private static final Logger LOGGER = Logger.getLogger(FileFieldEditorPanel.class.getName());
    private IndicatingAjaxSubmitLink uploadLink;
    private final Callback callback;
    private FileUploadField fileUploadField;
    private Model<String> fileNameModel;

    public interface Callback extends Serializable {
        void onUpload(String fileName, InputStream inputStream);
    }

    public FileFieldEditorPanel(String id, FieldEditorModel fieldEditorModel, Callback callback) {
        super(id, fieldEditorModel);
        this.callback = callback;
        setOutputMarkupId(true);
        addComponents();
    }

    private void addComponents() {
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        fileNameModel = new Model<String>() {
            @Override
            public String getObject() {
                return (String)getFieldEditorModel().getValue();
            }

            @Override
            public void setObject(String object) {
                getFieldEditorModel().setValue(object);
            }
        };
        Label fileNameLabel = new Label("fileName", fileNameModel);
        add(fileNameLabel);

        fileUploadField = new FileUploadField("fileInput");
        add(fileUploadField);

        uploadLink = new IndicatingAjaxSubmitLink("upload", (Form)getParent()) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    processUpload(fileUploadField.getFileUpload());
                    target.add(FileFieldEditorPanel.this);
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, null, t);
                }
            }
        };
        add(uploadLink);
    }

    private void processUpload(FileUpload upload) throws IOException {
        if (upload != null) {
            String fileName = upload.getClientFileName();
            InputStream inputStream = upload.getInputStream();
            callback.onUpload(fileName, inputStream);
            fileNameModel.setObject(fileName);
        }
    }


    @Override
    public void enableEditor(boolean editable) {
        fileUploadField.setEnabled(editable);
        uploadLink.setEnabled(editable);
    }

}
