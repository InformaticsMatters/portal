package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StringFieldEditorPanel extends FieldEditorPanel<String> {

    private static final Logger LOG = Logger.getLogger(StringFieldEditorPanel.class.getName());

    private TextField<String> textField;

    public StringFieldEditorPanel(String id, FieldEditorModel<String> fieldEditorModel) {
        super(id, fieldEditorModel);
        addComponents();
    }

    @Override
    public void enableEditor(boolean editable) {
        textField.setEnabled(editable);
    }

    private void addComponents() {
        Model<String> model = new Model<String>() {
            @Override
            public String getObject() {
                try {
                    return getFieldEditorModel().getValue();
                } catch (ClassCastException e) {
                    // can occur in rare case when datatype definitions change
                    String msg = "Value datatype is incompatible. Defaulting to null";
                    LOG.log(Level.WARNING, msg, e);
                    StringFieldEditorPanel.this.notify("Error", msg);
                    return null;
                }
            }

            @Override
            public void setObject(String object) {
                getFieldEditorModel().setValue(object);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        textField = new TextField<>("value", model);
        add(textField);
    }
}
