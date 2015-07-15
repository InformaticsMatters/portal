package portal.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.service.ServiceDescriptor;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author simetrias
 */
public class ServicesPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "service";
    private WebMarkupContainer servicesContainer;
    private ListView<ServiceDescriptor> listView;
    private Form<SearchServiceData> searchServiceForm;
    @Inject
    private ServiceDiscoverySession serviceDiscoverySession;

    public ServicesPanel(String id) {
        super(id);
        addSearchForm();
        addServices();
        refreshServiceList();
    }

    private void addServices() {
        servicesContainer = new WebMarkupContainer("servicesContainer");
        servicesContainer.setOutputMarkupId(true);

        listView = new ListView<ServiceDescriptor>("descriptors", new ArrayList<>()) {

            @Override
            protected void populateItem(ListItem<ServiceDescriptor> listItem) {
                ServiceDescriptor serviceDescriptor = listItem.getModelObject();
                listItem.add(new Label("name", serviceDescriptor.getName()));

                listItem.setOutputMarkupId(true);
                listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_TYPE, DROP_DATA_TYPE_VALUE));
                listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_ID, serviceDescriptor.getId().toString()));
            }
        };
        servicesContainer.add(listView);

        add(servicesContainer);
    }

    private void addSearchForm() {
        searchServiceForm = new Form<>("form");
        searchServiceForm.setModel(new CompoundPropertyModel<>(new SearchServiceData()));
        searchServiceForm.setOutputMarkupId(true);

        TextField<String> nameField = new TextField<>("pattern");
        searchServiceForm.add(nameField);

        searchServiceForm.add(new CheckBox("freeOnly"));

        AjaxSubmitLink searchAction = new IndicatingAjaxSubmitLink("search") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                refreshServiceList();
            }
        };
        searchServiceForm.add(searchAction);

        add(searchServiceForm);
    }

    private void refreshServiceList() {
        ServiceFilterData serviceFilterData = new ServiceFilterData();
        SearchServiceData searchServiceData = searchServiceForm.getModelObject();
        serviceFilterData.setPattern(searchServiceData.getPattern());
        serviceFilterData.setFreeOnly(searchServiceData.getFreeOnly());
        listView.setList(serviceDiscoverySession.listServices(serviceFilterData));
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        if (target != null) {
            target.add(servicesContainer);
            target.appendJavaScript("makeCardsDraggable()");
        }
    }
}
