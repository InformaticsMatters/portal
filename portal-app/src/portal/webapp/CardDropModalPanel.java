package portal.webapp;

import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.ServicePropertyDescriptor;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import toolkit.wicket.semantic.SemanticModalPanel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author simetrias
 */
public class CardDropModalPanel extends SemanticModalPanel {

    private final ServiceDescriptor serviceDescriptor;
    private Callbacks callbacks;
    private Map<ServicePropertyDescriptor, String> servicePropertyValueMap;
    private Form form;
    private String outputFileName;
    private Boolean createOutputFile = true;

    public CardDropModalPanel(String id, String modalElementWicketId, ServiceDescriptor serviceDescriptor) {
        super(id, modalElementWicketId);
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        this.serviceDescriptor = serviceDescriptor;
        addForm();
        addServiceProperties();
    }

    private void addForm() {
        form = new Form("form");

        TextField<String> outputFileNameField = new TextField<>("outputFileName", new PropertyModel<>(this, "outputFileName"));
        form.add(outputFileNameField);

        CheckBox outputFileNameCheck = new CheckBox("createOutputFile", new PropertyModel<>(this, "createOutputFile"));
        form.add(outputFileNameCheck);

        getModalRootComponent().add(form);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onCancel();
            }
        };
        form.add(cancelAction);
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    private void addServiceProperties() {
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
        } else {
            listItem.add(new StringPropertyEditorPanel("editor", servicePropertyDescriptor, servicePropertyModel));
        }
    }

    public interface Callbacks extends Serializable {

        void onCancel();

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
