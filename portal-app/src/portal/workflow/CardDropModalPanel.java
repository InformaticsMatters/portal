package portal.workflow;

import com.im.lac.services.ServiceDescriptor;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.types.AbstractStructure;
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
    private Map<OptionDescriptor, String> servicePropertyValueMap;
    private Form form;
    private String outputFileName;
    private Boolean createOutputFile = true;

    public CardDropModalPanel(String id, ServiceDescriptor serviceDescriptor) {
        super(id, "modalElement");
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

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onCancel();
            }
        };
        form.add(cancelAction);
        getModalRootComponent().add(form);
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    private void addServiceProperties() {
        OptionDescriptor[] parameters = serviceDescriptor.getAccessModes()[0].getParameters();
        createServicePropertyValueMap(parameters);
        ArrayList<OptionDescriptor> servicePropertyDescriptorList = new ArrayList<>(servicePropertyValueMap.keySet());

        ListView<OptionDescriptor> listView = new ListView<OptionDescriptor>("property", servicePropertyDescriptorList) {

            @Override
            protected void populateItem(ListItem<OptionDescriptor> listItem) {
                addServiceProperty(listItem);
            }
        };
        form.add(listView);
    }

    private void createServicePropertyValueMap(OptionDescriptor[] descriptors) {
        servicePropertyValueMap = new HashMap<>();
        if (descriptors != null && descriptors.length > 0) {
            for (OptionDescriptor descriptor : descriptors) {
                servicePropertyValueMap.put(descriptor, null);
            }
        }
    }

    private void addServiceProperty(ListItem<OptionDescriptor> listItem) {
        OptionDescriptor servicePropertyDescriptor = listItem.getModelObject();
        ServicePropertyModel servicePropertyModel = new ServicePropertyModel(servicePropertyDescriptor);
        if (servicePropertyDescriptor.getTypeDescriptor().getType() == String.class) {
            listItem.add(new StringPropertyEditorPanel("editor", servicePropertyDescriptor, servicePropertyModel));
        } else if (servicePropertyDescriptor.getTypeDescriptor().getType().isAssignableFrom(AbstractStructure.class) ) {
            listItem.add(new StructurePropertyEditorPanel("editor", "cardDropMarvinEditor", servicePropertyDescriptor, servicePropertyModel));
        } else {
            listItem.add(new StringPropertyEditorPanel("editor", servicePropertyDescriptor, servicePropertyModel));
        }
    }

    public interface Callbacks extends Serializable {

        void onCancel();

    }

    private class ServicePropertyModel implements IModel<String> {

        private final OptionDescriptor servicePropertyDescriptor;

        public ServicePropertyModel(OptionDescriptor servicePropertyDescriptor) {
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
