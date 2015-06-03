package portal.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.service.api.ServiceDescriptor;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class ServicesPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "service";

    private Form<BusquedaServicesData> form;

    @Inject
    private ServiceDiscoverySession serviceDiscoverySession;

    public ServicesPanel(String id) {
        super(id);
        addServices();
        addForm();
    }

    private void addServices() {
        serviceDiscoverySession.loadServices();
        ListView<ServiceDescriptor> listView = new ListView<ServiceDescriptor>("descriptors", serviceDiscoverySession.getServiceDescriptorList()) {

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

    private void addForm() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new BusquedaServicesData()));
        form.setOutputMarkupId(true);
        add(form);

        TextField<String> nameField = new TextField<>("name");
        form.add(nameField);

        form.add(new CheckBox("freeOnly"));

    }
}
