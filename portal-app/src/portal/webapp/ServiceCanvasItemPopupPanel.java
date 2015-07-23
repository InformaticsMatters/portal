package portal.webapp;

import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.ServicePropertyDescriptor;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPopupPanel extends Panel {

    private final ServiceCanvasItemData serviceCanvasItemData;
    private Map<ServicePropertyDescriptor, String> servicePropertyValueMap;
    private Form form;
    private Callbacks callbacks;

    public ServiceCanvasItemPopupPanel(String id, ServiceCanvasItemData serviceCanvasItemData, Callbacks callbacks) {
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
        ServiceDescriptor serviceDescriptor = serviceCanvasItemData.getServiceDescriptor();
        ServicePropertyDescriptor[] parameters = serviceDescriptor.getAccessModes()[0].getParameters();
        createServicePropertyValueMap(parameters);
        ArrayList<ServicePropertyDescriptor> servicePropertyDescriptorList = new ArrayList<>(servicePropertyValueMap.keySet());

        ListView<ServicePropertyDescriptor> listView = new ListView<ServicePropertyDescriptor>("property", servicePropertyDescriptorList) {

            @Override
            protected void populateItem(ListItem<ServicePropertyDescriptor> listItem) {
                addServiceProperty(listItem);
            }
        };
        form.add(listView);
    }

    private void createServicePropertyValueMap(ServicePropertyDescriptor[] descriptors) {
        servicePropertyValueMap = new HashMap<>();
        if (descriptors != null && descriptors.length > 0) {
            for (ServicePropertyDescriptor descriptor : descriptors) {
                servicePropertyValueMap.put(descriptor, null);
            }
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

    public interface Callbacks extends Serializable {

        void onDelete();

        void onSave();

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
