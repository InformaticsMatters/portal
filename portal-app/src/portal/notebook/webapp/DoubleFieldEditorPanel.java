package portal.notebook.webapp;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public class DoubleFieldEditorPanel extends FieldEditorPanel {
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");


    public DoubleFieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
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
                try {
                    Double value = asDouble(getFieldEditorModel().getValue());
                    if (value == null) {
                        return null;
                    } else {
                        return decimalFormat.format(value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void setObject(String object) {
                try {
                    if (object == null) {
                        getFieldEditorModel().setValue(null);
                    } else {
                        getFieldEditorModel().setValue(decimalFormat.parse(object).doubleValue());
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

    private Double asDouble(Object value) throws Exception {
        if (value == null) {
            return null;
        } else if (value.getClass().equals(Double.class)) {
            return (Double)value;
        } else if (value.getClass().equals(Float.class)) {
            return ((Float)value).doubleValue();
        } else if (value.getClass().equals(Integer.class)) {
            return ((Integer)value).doubleValue();
        } else if (value.getClass().equals(String.class)) {
            return decimalFormat.parse((String)value).doubleValue();
        } else if (value.getClass().equals(BigDecimal.class)) {
            return ((BigDecimal)value).doubleValue();
        } else {
            throw new Exception("Unsupported conversion from " + value.getClass());
        }
    }

}
