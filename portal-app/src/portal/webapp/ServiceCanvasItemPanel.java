package portal.webapp;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPanel extends Panel {

    private ServiceCanvasItemPopupPanel serviceCanvasItemPopup;
    private Callbacks callbacks;

    public ServiceCanvasItemPanel(String id, ServiceCanvasItemData serviceCanvasItemData, Callbacks callbacks) {
        super(id);
        setOutputMarkupId(true);
        this.callbacks = callbacks;
        addServiceCanvasItemPopup();

        add(new Label("id", serviceCanvasItemData.getServiceDescriptor().getId()));
        add(new Label("name", serviceCanvasItemData.getServiceDescriptor().getName()));

        add(new AjaxEventBehavior("click") {

            @Override
            protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                serviceCanvasItemPopup.setVisible(true);
                ajaxRequestTarget.add(serviceCanvasItemPopup);
                String js = "$('#" + getMarkupId() + "').popup({popup: '.ui.serviceCanvasItemPopup.popup', on : 'click'}).popup('toggle')";
                ajaxRequestTarget.appendJavaScript(js);
            }
        });
    }

    private void addServiceCanvasItemPopup() {
        serviceCanvasItemPopup = new ServiceCanvasItemPopupPanel("serviceCanvasItemPopup", callbacks);
        serviceCanvasItemPopup.setVisible(false);
        add(serviceCanvasItemPopup);
    }

    public interface Callbacks extends Serializable {

        void onDelete();

    }
}
