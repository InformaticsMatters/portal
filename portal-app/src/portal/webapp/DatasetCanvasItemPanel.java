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
public class DatasetCanvasItemPanel extends Panel {

    private final DatasetCanvasItemData data;
    private final Callbacks callbacks;
    private DatasetPopupPanel popupPanel;

    @Inject
    private PopupContainerProvider popupContainerProvider;
    private AjaxLink openPopupLink;

    public DatasetCanvasItemPanel(String id, DatasetCanvasItemData data, Callbacks callbacks) {
        super(id);
        this.data = data;
        this.callbacks = callbacks;
        setOutputMarkupId(true);

        createPopupPanel();

        add(new Label("description", data.getDatasetDescriptor().getDescription()));
        add(new Label("rowCount", data.getDatasetDescriptor().getRowCount()));

        openPopupLink = new AjaxLink("openPopup") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                popupContainerProvider.setPopupContentForPage(getPage(), popupPanel);
                popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
                String js = "$('#:link')" +
                        ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.datasetPopup.popup'), on : 'click'})" +
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
                callbacks.onDatasetCanvasItemDelete();
            }
        });
    }

    private void createPopupPanel() {
        popupPanel = new DatasetPopupPanel("content", data.getDatasetDescriptor());
    }

    public DatasetCanvasItemData getData() {
        return data;
    }

    public interface Callbacks extends Serializable {

        void onDatasetCanvasItemDelete();

    }
}
