package portal.notebook.webapp;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoubleFieldEditorPanel extends FieldEditorPanel<Double> {
    private static final Logger LOG = Logger.getLogger(DoubleFieldEditorPanel.class.getName());
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private TextField<String> textField;


    public DoubleFieldEditorPanel(String id, FieldEditorModel<Double> fieldEditorModel) {
        super(id, fieldEditorModel);
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(symbols);
        addComponents();
    }

    @Override
    public void enableEditor(boolean editable) {
        textField.setEnabled(editable);
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
                } catch (Throwable e) {
                    String msg = "Error converting " + getFieldEditorModel().getValue();
                    LOG.log(Level.WARNING, msg, e);
                    return null;
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
                } catch (Throwable e) {
                    LOG.log(Level.WARNING, "Error converting " + object, e);
                    DoubleFieldEditorPanel.this.notify("Error", e.getMessage());
                }
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        textField = new TextField<>("value", propertyModel);
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
            String msg = "Unsupported conversion from " + value.getClass();
            LOG.warning(msg);
            notify("Error", msg);
            return null;
        }
    }

}
