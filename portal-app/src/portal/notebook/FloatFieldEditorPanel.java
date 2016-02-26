package portal.notebook;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class FloatFieldEditorPanel extends FieldEditorPanel {
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");


    public FloatFieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
        super(id, fieldEditorModel);
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(symbols);
        addComponents();
    }

    private void addComponents() {
        Model<String> propertyModel = new Model<String>() {
            @Override
            public String getObject() {
                Float value = (Float)getFieldEditorModel().getValue();
                if (value == null) {
                    return null;
                } else {
                    return decimalFormat.format(value);
                }
            }

            @Override
            public void setObject(String object) {
                try {
                    if (object == null) {
                        getFieldEditorModel().setValue(null);
                    } else {
                        getFieldEditorModel().setValue(decimalFormat.parse(object).floatValue());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        TextField<String> textField = new TextField<>("value", propertyModel);
        add(textField);
    }

}
