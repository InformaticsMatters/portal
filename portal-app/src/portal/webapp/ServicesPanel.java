package portal.webapp;

import org.apache.wicket.AttributeModifier;
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

    public static final String DROP_DATA_TYPE_VALUE = "service";

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

                listItem.setOutputMarkupId(true);
                listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_TYPE, DROP_DATA_TYPE_VALUE));
                listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_ID, serviceDescriptor.getId().toString()));
            }
        };
        add(listView);
    }
}
