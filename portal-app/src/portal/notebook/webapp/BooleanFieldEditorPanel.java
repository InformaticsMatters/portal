package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.Model;

public class BooleanFieldEditorPanel extends FieldEditorPanel {

    public BooleanFieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
        super(id, fieldEditorModel);
        addComponents();
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
        CheckBox checkBox = new CheckBox("value", model);
        add(checkBox);
    }
}
