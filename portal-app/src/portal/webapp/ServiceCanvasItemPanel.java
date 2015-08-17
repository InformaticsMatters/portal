package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPanel extends Panel {

    private final ServiceCanvasItemData data;
    private final AjaxLink openPopupLink;
    private ServiceCanvasItemPopupPanel popupPanel;
    private Callbacks callbacks;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public ServiceCanvasItemPanel(String id, ServiceCanvasItemData data, Callbacks callbacks) {
        super(id);
        this.callbacks = callbacks;
        this.data = data;
        setOutputMarkupId(true);
        createPopupPanel();

        add(new Label("name", data.getServiceDescriptor().getName()));

        openPopupLink = new AjaxLink("openPopup") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                popupContainerProvider.setPopupContentForPage(getPage(), popupPanel);
                popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
                String js = "$('#" + openPopupLink.getMarkupId() + "').popup({simetriasPatch: true, popup: $('#" + popupPanel.getMarkupId() + "').find('.ui.serviceCanvasItemPopup.popup'), on : 'click'}).popup('toggle')";
                ajaxRequestTarget.appendJavaScript(js);
            }
        };
        add(openPopupLink);

        add(new AjaxLink("delete") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                popupContainerProvider.refreshContainer(getPage(), getRequestCycle().find(AjaxRequestTarget.class));
                callbacks.onServiceCanvasItemDelete();
            }
        });
    }

    private void createPopupPanel() {
        popupPanel = new ServiceCanvasItemPopupPanel("content", data, new ServiceCanvasItemPopupPanel.Callbacks() {

            @Override
            public void onDelete() {

            }

            @Override
            public void onSave() {
                callbacks.onServiceCanvasItemSave();
                String js = "$('#" + openPopupLink.getMarkupId() + "').popup({simetriasPatch: true, popup: $('#" + popupPanel.getMarkupId() + "').find('.ui.serviceCanvasItemPopup.popup'), on : 'click'}).popup('toggle')";
                getRequestCycle().find(AjaxRequestTarget.class).appendJavaScript(js);
            }
        });
    }

    public ServiceCanvasItemData getData() {
        return data;
    }

    public interface Callbacks extends Serializable {

        void onServiceCanvasItemDelete();

        void onServiceCanvasItemSave();

    }
}
