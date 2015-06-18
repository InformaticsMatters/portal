package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPopupPanel extends Panel {

    private final ServiceCanvasItemData serviceCanvasItemData;
    private ServiceCanvasItemPanel.Callbacks callbacks;
    private Map<ServicePropertyDescriptor, String> servicePropertyValueMap;

    public ServiceCanvasItemPopupPanel(String id, ServiceCanvasItemData serviceCanvasItemData, ServiceCanvasItemPanel.Callbacks callbacks) {
        super(id);
        this.callbacks = callbacks;
        this.serviceCanvasItemData = serviceCanvasItemData;
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        addServiceProperties();
        addActions();
    }

    private void addServiceProperties() {
        List<ServicePropertyDescriptor> servicePropertyDescriptorList = serviceCanvasItemData.getServiceDescriptor().getServicePropertyDescriptorList();
        createServicePropertyValueMap(servicePropertyDescriptorList);
        ListView<ServicePropertyDescriptor> listView = new ListView<ServicePropertyDescriptor>("property", servicePropertyDescriptorList) {

            @Override
            protected void populateItem(ListItem<ServicePropertyDescriptor> listItem) {
                addServiceProperty(listItem);
            }
        };
        add(listView);
    }

    private void createServicePropertyValueMap(List<ServicePropertyDescriptor> servicePropertyDescriptorList) {
        servicePropertyValueMap = new HashMap<>(servicePropertyDescriptorList.size());
        for (ServicePropertyDescriptor descriptor : servicePropertyDescriptorList) {
            servicePropertyValueMap.put(descriptor, null);
        }
    }

    private void addServiceProperty(ListItem<ServicePropertyDescriptor> listItem) {
        ServicePropertyDescriptor servicePropertyDescriptor = listItem.getModelObject();
        if (ServicePropertyDescriptor.Type.STRING == servicePropertyDescriptor.getType()) {
            listItem.add(new StringPropertyEditorPanel("editor", servicePropertyDescriptor));
        } else if (ServicePropertyDescriptor.Type.STRUCTURE == servicePropertyDescriptor.getType()) {
            listItem.add(new StructurePropertyEditorPanel("editor", servicePropertyDescriptor));
        }
    }

    private void addActions() {
        add(new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onSave();
            }
        });
        add(new AjaxLink("delete") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onDelete();
            }
        });
    }
}
