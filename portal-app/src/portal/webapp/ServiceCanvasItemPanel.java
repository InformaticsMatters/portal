package portal.webapp;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPanel extends Panel {

    private ServiceCanvasItemPopupPanel serviceCanvasItemPopup;

    public ServiceCanvasItemPanel(String id, ServiceCanvasItemData model) {
        super(id);
        setOutputMarkupId(true);
        addServiceCanvasItemPopup();

        add(new Label("id", model.getServiceDescriptor().getId()));
        add(new Label("name", model.getServiceDescriptor().getName()));

        add(new AjaxEventBehavior("click") {
            @Override
            protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                // serviceCanvasItemPopup.setDefaultModelObject(serviceDescriptor);
                serviceCanvasItemPopup.setVisible(true);
                ajaxRequestTarget.add(serviceCanvasItemPopup);
                String js = "$('.canvas-item-content').popup({popup: '.ui.serviceCanvasItemPopup.popup', on : 'click'}).popup('toggle')";
                ajaxRequestTarget.appendJavaScript(js);
            }
        });
    }

    private void addServiceCanvasItemPopup() {
        serviceCanvasItemPopup = new ServiceCanvasItemPopupPanel("serviceCanvasItemPopup");
        // serviceCanvasItemPopup.setDefaultModel(new CompoundPropertyModel<>(null));
        serviceCanvasItemPopup.setVisible(false);
        add(serviceCanvasItemPopup);
    }
}
