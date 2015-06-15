package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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
        addServiceCanvasItemPopup(serviceCanvasItemData);

        add(new Label("id", serviceCanvasItemData.getServiceDescriptor().getId()));
        add(new Label("name", serviceCanvasItemData.getServiceDescriptor().getName()));

        add(new AjaxLink("openPopup") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                serviceCanvasItemPopup.setVisible(true);
                ajaxRequestTarget.add(serviceCanvasItemPopup);
                String js = "$('#" + ServiceCanvasItemPanel.this.getMarkupId() + "').popup({popup: $('#" + ServiceCanvasItemPanel.this.getMarkupId() + "').find('.ui.serviceCanvasItemPopup.popup'), on : 'click'}).popup('toggle')";
                ajaxRequestTarget.appendJavaScript(js);
            }
        });
    }

    private void addServiceCanvasItemPopup(ServiceCanvasItemData serviceCanvasItemData) {
        serviceCanvasItemPopup = new ServiceCanvasItemPopupPanel("serviceCanvasItemPopup", serviceCanvasItemData, callbacks);
        serviceCanvasItemPopup.setVisible(false);
        add(serviceCanvasItemPopup);
    }

    public interface Callbacks extends Serializable {

        void onDelete();

    }
}
