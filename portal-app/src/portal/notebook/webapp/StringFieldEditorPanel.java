package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

public class StringFieldEditorPanel extends FieldEditorPanel<String> {

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
                return (String)getFieldEditorModel().getValue();
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
