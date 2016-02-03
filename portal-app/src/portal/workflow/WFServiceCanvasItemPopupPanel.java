package portal.workflow;

import com.im.lac.services.ServiceDescriptor;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author simetrias
 */
public class WFServiceCanvasItemPopupPanel extends Panel {

    private final WFServiceCanvasItemData serviceCanvasItemData;
    private Map<OptionDescriptor, String> servicePropertyValueMap;
    private Form form;
    private Callbacks callbacks;
    private String outputFileName;
    private Boolean createOutputFile = true;

    public WFServiceCanvasItemPopupPanel(String id, WFServiceCanvasItemData serviceCanvasItemData, Callbacks callbacks) {
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

        TextField<String> outputFileNameField = new TextField<>("outputFileName", new PropertyModel<>(this, "outputFileName"));
        form.add(outputFileNameField);

        CheckBox outputFileNameCheck = new CheckBox("createOutputFile", new PropertyModel<>(this, "createOutputFile"));
        form.add(outputFileNameCheck);

        add(form);
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public Boolean getCreateOutputFile() {
        return createOutputFile;
    }

    public void setCreateOutputFile(Boolean createOutputFile) {
        this.createOutputFile = createOutputFile;
    }

    private void addServiceProperties() {
        ServiceDescriptor serviceDescriptor = serviceCanvasItemData.getServiceDescriptor();
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
        } else if (servicePropertyDescriptor.getTypeDescriptor().getType() == MoleculeTypeDescriptor.class) {
            listItem.add(new StructurePropertyEditorPanel("editor", "canvasMarvinEditor", servicePropertyDescriptor, servicePropertyModel));
        } else if (servicePropertyDescriptor.getTypeDescriptor().getType() == Boolean.class) {
            listItem.add(new BooleanPropertyEditorPanel("editor", servicePropertyDescriptor, servicePropertyModel));
        } else {
            listItem.add(new StringPropertyEditorPanel("editor", servicePropertyDescriptor, servicePropertyModel));
        }
    }

    private void addActions() {
        form.add(new IndicatingAjaxSubmitLink("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                serviceCanvasItemData.setServicePropertyValueMap(servicePropertyValueMap);
                serviceCanvasItemData.setCreateOutputFile(createOutputFile);
                serviceCanvasItemData.setOutputFileName(outputFileName);
                callbacks.onSave();
            }
        });
    }


    public interface Callbacks extends Serializable {

        void onSave();

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
