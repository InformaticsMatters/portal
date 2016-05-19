package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.Model;

public class BooleanFieldEditorPanel extends FieldEditorPanel {

    private CheckBox checkBox;

    public BooleanFieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
        super(id, fieldEditorModel);
        addComponents();
    }

    @Override
    public void enableEditor(boolean editable) {
        checkBox.setEnabled(editable);
    }

    private void addComponents() {
        Model<Boolean> model = new Model<Boolean>() {
            @Override
            public Boolean getObject() {
                return (Boolean)getFieldEditorModel().getValue();
            }

            @Override
            public void setObject(Boolean object) {
                getFieldEditorModel().setValue(object);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        checkBox = new CheckBox("value", model);
        add(checkBox);
    }
}
