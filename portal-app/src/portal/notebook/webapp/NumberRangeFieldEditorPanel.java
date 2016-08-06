package portal.notebook.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.NumberRange;

/**
 * @author Tim Dudgeon
 */
public class NumberRangeFieldEditorPanel<T extends Number & Comparable<T>> extends FieldEditorPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NumberRangeFieldEditorPanel.class.getName());

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

        OptionDescriptor<? extends NumberRange> optionDescriptor = fieldEditorModel.getOptionDescriptor();
        Class cls = optionDescriptor.getTypeDescriptor().getType();
        NumberRange range = (NumberRange)fieldEditorModel.getValue();
        if (range == null) {
            range = NumberRange.create(cls);
        }

        CompoundPropertyModel<NumberRange> model = new CompoundPropertyModel<>(range);

        // TODO - allow the OptionDescriptor to specify min and max bounds
        Label label = new Label("label", fieldEditorModel.getDisplayName());
        label.add(new AttributeModifier("title", optionDescriptor.getDescription()));
        minField = new NumberTextField<T>("minValue", model.bind("minValue"), range.getType());
        maxField = new NumberTextField<T>("maxValue", model.bind("maxValue"), range.getType());
        add(label);
        add(minField);
        add(maxField);
    }


}
