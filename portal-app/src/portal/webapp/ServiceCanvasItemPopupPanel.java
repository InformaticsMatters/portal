package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPopupPanel extends Panel {

    private final ServiceCanvasItemData serviceCanvasItemData;
    private ServiceCanvasItemPanel.Callbacks callbacks;

    public ServiceCanvasItemPopupPanel(String id, ServiceCanvasItemData serviceCanvasItemData, ServiceCanvasItemPanel.Callbacks callbacks) {
        super(id);
        this.serviceCanvasItemData = serviceCanvasItemData;
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        addProperties();
        addActions(callbacks);
    }

    private void addProperties() {
        List<ServicePropertyDescriptor> servicePropertyDescriptorList = serviceCanvasItemData.getServiceDescriptor().getServicePropertyDescriptorList();
        ListView<ServicePropertyDescriptor> listView = new ListView<ServicePropertyDescriptor>("property", servicePropertyDescriptorList) {

            @Override
            protected void populateItem(ListItem<ServicePropertyDescriptor> listItem) {
                addServiceProperty(listItem);
            }
        };
        add(listView);
    }

    private void addServiceProperty(ListItem<ServicePropertyDescriptor> listItem) {
        ServicePropertyDescriptor object = listItem.getModelObject();
        if (ServicePropertyDescriptor.Type.STRING == object.getType()) {
            listItem.add(new StringPropertyEditorPanel("editor", object));
        } else if (ServicePropertyDescriptor.Type.STRUCTURE == object.getType()) {
            listItem.add(new StructurePropertyEditorPanel("editor", object));
        }
    }

    private void addActions(final ServiceCanvasItemPanel.Callbacks callbacks) {
        add(new AjaxLink("delete") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onDelete();
            }
        });
    }
}
