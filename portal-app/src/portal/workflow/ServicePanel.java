package portal.workflow;

import org.squonk.core.ServiceDescriptor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class ServicePanel extends Panel {

    private final ServiceDescriptor serviceDescriptor;

    public ServicePanel(String id, ServiceDescriptor serviceDescriptor) {
        super(id);
        this.serviceDescriptor = serviceDescriptor;
        addComponents();
    }

    private void addComponents() {
        add(new Label("name", serviceDescriptor.getName()));
    }
}
