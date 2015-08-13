package portal.webapp;

import com.im.lac.services.ServiceDescriptor;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
public class ServicesPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "service";
    private WebMarkupContainer servicesContainer;
    private ListView<ServiceItemData> listView;
    private Form<SearchServiceData> searchServiceForm;
    @Inject
    private ServicesSession servicesSession;

    public ServicesPanel(String id) {
        super(id);
        addSearchForm();
        addServices();
        refreshServiceList();
    }

    private void addServices() {
        servicesContainer = new WebMarkupContainer("servicesContainer");
        servicesContainer.setOutputMarkupId(true);

        listView = new ListView<ServiceItemData>("descriptors", new ArrayList<>()) {

            @Override
            protected void populateItem(ListItem<ServiceItemData> listItem) {
                ServiceItemData listItemData = listItem.getModelObject();
                listItem.setOutputMarkupId(true);
                if (listItemData.isFolder()) {
                    listItem.add(new FolderPanel("item"));
                } else {
                    ServiceDescriptor serviceDescriptor = listItemData.getServiceDescriptor();
                    listItem.add(new ServicePanel("item", serviceDescriptor));
                    listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_TYPE, DROP_DATA_TYPE_VALUE));
                    listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_ID, serviceDescriptor.getId().replace('/', '_')));
                }
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
        List<ServiceDescriptor> serviceDescriptors = servicesSession.listServiceDescriptors();
        List<ServiceItemData> dataList = buildServiceItemDataList(serviceDescriptors);
        listView.setList(dataList);
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        if (target != null) {
            target.add(servicesContainer);
        }
    }

    private List<ServiceItemData> buildServiceItemDataList(List<ServiceDescriptor> serviceDescriptors) {
        List<ServiceItemData> result = new ArrayList<>();
        for (ServiceDescriptor serviceDescriptor : serviceDescriptors) {
            ServiceItemData data = new ServiceItemData();
            data.setIsFolder(false);
            data.setServiceDescriptor(serviceDescriptor);
            result.add(data);
        }

        ServiceItemData folder = new ServiceItemData();
        folder.setIsFolder(true);
        folder.setFolderName("Folder name");
        result.add(folder);

        return result;
    }

    private class ServiceItemData implements Serializable {

        private boolean isFolder;
        private String folderName;
        private ServiceDescriptor serviceDescriptor;

        public boolean isFolder() {
            return isFolder;
        }

        public void setIsFolder(boolean isFolder) {
            this.isFolder = isFolder;
        }

        public String getFolderName() {
            return folderName;
        }

        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }

        public ServiceDescriptor getServiceDescriptor() {
            return serviceDescriptor;
        }

        public void setServiceDescriptor(ServiceDescriptor serviceDescriptor) {
            this.serviceDescriptor = serviceDescriptor;
        }
    }
}
