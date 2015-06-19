package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

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
    private Form form;

    public ServiceCanvasItemPopupPanel(String id, ServiceCanvasItemData serviceCanvasItemData, ServiceCanvasItemPanel.Callbacks callbacks) {
        super(id);
        this.callbacks = callbacks;
        this.serviceCanvasItemData = serviceCanvasItemData;
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        addForm();
        addServiceProperties();
        addActions();
    }

    private void addForm() {
        form = new Form("form");
        add(form);
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
        form.add(listView);
    }

    private void createServicePropertyValueMap(List<ServicePropertyDescriptor> servicePropertyDescriptorList) {
        servicePropertyValueMap = new HashMap<>(servicePropertyDescriptorList.size());
        for (ServicePropertyDescriptor descriptor : servicePropertyDescriptorList) {
            servicePropertyValueMap.put(descriptor, null);
        }
    }

    private void addServiceProperty(ListItem<ServicePropertyDescriptor> listItem) {
        ServicePropertyDescriptor servicePropertyDescriptor = listItem.getModelObject();
        ServicePropertyModel servicePropertyModel = new ServicePropertyModel(servicePropertyDescriptor);
        if (ServicePropertyDescriptor.Type.STRING == servicePropertyDescriptor.getType()) {
            listItem.add(new StringPropertyEditorPanel("editor", servicePropertyDescriptor, servicePropertyModel));
        } else if (ServicePropertyDescriptor.Type.STRUCTURE == servicePropertyDescriptor.getType()) {
            listItem.add(new StructurePropertyEditorPanel("editor", servicePropertyDescriptor, servicePropertyModel));
        }
    }

    private void addActions() {
        form.add(new IndicatingAjaxSubmitLink("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                for (ServicePropertyDescriptor servicePropertyDescriptor : servicePropertyValueMap.keySet()) {
                    System.out.println(servicePropertyDescriptor.getLabel() + ": " + servicePropertyValueMap.get(servicePropertyDescriptor));
                }
                callbacks.onSave();
            }
        });

        form.add(new AjaxLink("delete") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onDelete();
            }
        });
    }

    private class ServicePropertyModel implements IModel<String> {

        private final ServicePropertyDescriptor servicePropertyDescriptor;

        public ServicePropertyModel(ServicePropertyDescriptor servicePropertyDescriptor) {
            this.servicePropertyDescriptor = servicePropertyDescriptor;
        }

        @Override
        public String getObject() {
            return servicePropertyValueMap.get(servicePropertyDescriptor);
        }

        @Override
        public void setObject(String s) {
            servicePropertyValueMap.put(servicePropertyDescriptor, s);
        }

        @Override
        public void detach() {
        }
    }
}
