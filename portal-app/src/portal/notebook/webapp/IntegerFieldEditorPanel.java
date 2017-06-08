package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class IntegerFieldEditorPanel extends FieldEditorPanel<Integer> {

    private static final Logger LOG = Logger.getLogger(IntegerFieldEditorPanel.class.getName());


    private final FieldEditorModel<Integer> fieldEditorModel;
    private NumberTextField<Integer> textField;

    public IntegerFieldEditorPanel(String id, FieldEditorModel<Integer> fieldEditorModel) {
        super(id, fieldEditorModel);
        this.fieldEditorModel = fieldEditorModel;
        addComponents();
    }

    @Override
    public void enableEditor(boolean editable) {
        textField.setEnabled(editable);
    }

    private void addComponents() {
        CompoundPropertyModel<IntegerModelObject> model = new CompoundPropertyModel<>(new IntegerModelObject());
        IModel<Integer> propertyModel = model.bind("value");
        add(new Label("label", fieldEditorModel.getDisplayName()));
        textField = new NumberTextField<>("value", propertyModel);
        add(textField);
    }

    class IntegerModelObject implements Serializable {

        public Integer getValue() {
            try {
                return getFieldEditorModel().getValue();
            } catch (ClassCastException e) {
                // can occur in rare case when datatype definitions change
                String msg = "Value datatype is incompatible. Defaulting to null";
                LOG.log(Level.WARNING, msg, e);
                return null;
            }
        }

        public void setValue(Integer value) {
            getFieldEditorModel().setValue(value);
        }
    }


}
