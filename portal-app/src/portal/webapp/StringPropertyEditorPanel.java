package portal.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * @author simetrias
 */
public class StringPropertyEditorPanel extends Panel {

    public StringPropertyEditorPanel(String id, ServicePropertyDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        super(id);
        addComponents(servicePropertyDescriptor, servicePropertyModel);
    }

    private void addComponents(ServicePropertyDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        add(new Label("label", servicePropertyDescriptor.getLabel()));
        TextField<String> stringTextField = new TextField<>("value", servicePropertyModel);
        add(stringTextField);
    }
}
