package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.Model;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BooleanFieldEditorPanel extends FieldEditorPanel<Boolean> {

    private static final Logger LOG = Logger.getLogger(BooleanFieldEditorPanel.class.getName());

    private CheckBox checkBox;

    public BooleanFieldEditorPanel(String id, FieldEditorModel<Boolean> fieldEditorModel) {
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
                try {
                    return getFieldEditorModel().getValue();
                } catch (ClassCastException e) {
                    // can occur in rare case when datatype definitions change
                    LOG.log(Level.WARNING, "Value datatype is incompatible. Defaulting to null", e);
                    return null;
                }
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
