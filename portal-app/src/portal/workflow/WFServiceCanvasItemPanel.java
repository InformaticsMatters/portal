package portal.workflow;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import portal.PopupContainerProvider;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class WFServiceCanvasItemPanel extends Panel {

    private final WFServiceCanvasItemData data;
    private final AjaxLink openPopupLink;
    private WFServiceCanvasItemPopupPanel popupPanel;
    private Callbacks callbacks;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public WFServiceCanvasItemPanel(String id, WFServiceCanvasItemData data, Callbacks callbacks) {
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
                String js = "$('#:link')" +
                        ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.serviceCanvasItemPopup.popup'), on : 'click'})" +
                        ".popup('toggle').popup('destroy')";
                js = js.replace(":link", openPopupLink.getMarkupId()).replace(":content", popupPanel.getMarkupId());
                System.out.println(js);
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
        popupPanel = new WFServiceCanvasItemPopupPanel("content", data, () -> {
            callbacks.onServiceCanvasItemSave();
            String js = "$('#" + openPopupLink.getMarkupId() + "').popup({simetriasPatch: true, popup: $('#" + popupPanel.getMarkupId() + "').find('.ui.serviceCanvasItemPopup.popup'), on : 'click'}).popup('toggle')";
            getRequestCycle().find(AjaxRequestTarget.class).appendJavaScript(js);
        });
    }

    public WFServiceCanvasItemData getData() {
        return data;
    }

    public interface Callbacks extends Serializable {

        void onServiceCanvasItemDelete();

        void onServiceCanvasItemSave();

    }
}