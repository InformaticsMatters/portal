package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.CompoundPropertyModel;

import java.io.Serializable;

/**
 * @author Tim Dudgeon
 */
public class NumberRangeFieldEditorPanel<T extends Number & Comparable<T>> extends FieldEditorPanel {


    private final FieldEditorModel fieldEditorModel;
    private NumberTextField<T> minField;
    private NumberTextField<T> maxField;

    public NumberRangeFieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
        super(id, fieldEditorModel);
        this.fieldEditorModel = fieldEditorModel;
        addComponents();
    }

    @Override
    public void enableEditor(boolean editable) {
        minField.setEnabled(editable);
        maxField.setEnabled(editable);
    }

    private void addComponents() {
        CompoundPropertyModel<NumberRangeModelObject> model = new CompoundPropertyModel<>(new NumberRangeModelObject());

        // TODO - need a way for the TypeDescriptor (available from the FieldEditorModel) to define the labels
        add(new Label("minLabel", fieldEditorModel.getDisplayName() + " min"));
        add(new Label("minLabel", fieldEditorModel.getDisplayName() + " max"));
        minField = new NumberTextField<T>("min", model.bind("minValue"));
        maxField = new NumberTextField<T>("max", model.bind("maxValue"));
        add(minField);
        add(maxField);
    }

    // TODO - this would be moved out to an API class so that values could be submitted to server.
    class NumberRangeModelObject implements Serializable {

        private T minValue;
        private T maxValue;

        public NumberRangeModelObject() {}
        public NumberRangeModelObject(T minValue, T maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public T getMinValue() { return minValue; }
        public void setMinValue(T value) { this.minValue = value; }

        public T getMaxValue() { return maxValue; }
        public void setMaxValue(T value) { this.maxValue = value; }
    }


}
