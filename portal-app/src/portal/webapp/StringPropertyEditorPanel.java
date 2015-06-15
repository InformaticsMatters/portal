package portal.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

/**
 * @author simetrias
 */
public class StringPropertyEditorPanel extends Panel {

    private final ServicePropertyDescriptor servicePropertyDescriptor;
    private TextField<String> stringTextField;
    private String value;

    public StringPropertyEditorPanel(String id, ServicePropertyDescriptor servicePropertyDescriptor) {
        super(id);
        this.servicePropertyDescriptor = servicePropertyDescriptor;
        addComponents();
    }

    private void addComponents() {
        add(new Label("label", servicePropertyDescriptor.getLabel()));
        stringTextField = new TextField<>("value", new PropertyModel<>(this, "value"));
        add(stringTextField);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
