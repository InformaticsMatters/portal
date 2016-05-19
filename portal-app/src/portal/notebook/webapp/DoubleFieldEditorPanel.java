package portal.notebook.webapp;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoubleFieldEditorPanel extends FieldEditorPanel {
    private static final Logger LOGGER = Logger.getLogger(DoubleFieldEditorPanel.class.getName());
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    @Inject
    private NotifierProvider notifierProvider;
    private TextField<String> textField;


    public DoubleFieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
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
                    LOGGER.log(Level.WARNING, "Error converting " + getFieldEditorModel().getValue(), e);
                    notifierProvider.getNotifier(getPage()).notify("Error", e.getMessage());
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
                    LOGGER.log(Level.WARNING, "Error converting " + object, e);
                    notifierProvider.getNotifier(getPage()).notify("Error", e.getMessage());
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
            throw new Exception("Unsupported conversion from " + value.getClass());
        }
    }

}
