package portal.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import portal.service.api.ServiceDescriptor;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class ServicesPanel extends Panel {

    private ListView<ServiceDescriptor> listView;
    @Inject
    private ServiceDiscoverySession serviceDiscoverySession;

    public ServicesPanel(String id) {
        super(id);
        addServices();
    }

    private void addServices() {
        serviceDiscoverySession.loadServices();
        listView = new ListView<ServiceDescriptor>("descriptors", serviceDiscoverySession.getServiceDescriptorList()) {

            @Override
            protected void populateItem(ListItem<ServiceDescriptor> listItem) {
                ServiceDescriptor serviceDescriptor = listItem.getModelObject();
                listItem.add(new Label("name", serviceDescriptor.getName()));
            }
        };
        add(listView);
    }
}
