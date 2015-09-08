package portal.webapp;

import com.im.lac.services.ServicePropertyDescriptor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Created by mariapaz on 9/7/15.
 */
public class BooleanPropertyEditorPanel extends Panel {

    public BooleanPropertyEditorPanel(String id, ServicePropertyDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        super(id);
        addComponents(servicePropertyDescriptor, servicePropertyModel);
    }

    private void addComponents(ServicePropertyDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        add(new Label("label", servicePropertyDescriptor.getLabel()));
        CheckBox nombre = new CheckBox("boolean", servicePropertyModel);
        add(nombre);
    }
}
