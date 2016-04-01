package portal.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class IntegerFieldEditorPanel extends FieldEditorPanel {


    private final FieldEditorModel fieldEditorModel;

    public IntegerFieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
        super(id, fieldEditorModel);
        this.fieldEditorModel = fieldEditorModel;
        addComponents();
    }

    private void addComponents() {
        CompoundPropertyModel<IntegerModelObject> model = new CompoundPropertyModel<>(new IntegerModelObject());
        IModel<Integer> propertyModel = model.bind("value");
        add(new Label("label", fieldEditorModel.getDisplayName()));
        NumberTextField<Integer> textField = new NumberTextField<>("value", propertyModel);
        add(textField);
    }

    class IntegerModelObject implements Serializable {

        public Integer getValue() {
            return (Integer)getFieldEditorModel().getValue();
        }

        public void setValue(Integer value) {
            getFieldEditorModel().setValue(value);
        }
    }


}
