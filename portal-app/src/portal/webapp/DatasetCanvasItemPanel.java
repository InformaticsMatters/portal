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
    private DatasetCanvasItemPopupPanel datasetCanvasItemPopupPanel;

    @Inject
    private PopupContainerProvider popupContainerProvider;
    private AjaxLink openPopupLink;

    public DatasetCanvasItemPanel(String id, DatasetCanvasItemData data, Callbacks callbacks) {
        super(id);
        this.data = data;
        this.callbacks = callbacks;
        setOutputMarkupId(true);

        createDatasetCanvasItemPopupPanel();

        add(new Label("id", data.getDatasetDescriptor().getId()));
        add(new Label("description", data.getDatasetDescriptor().getDescription()));
        add(new Label("rowCount", data.getDatasetDescriptor().getRowCount()));

        openPopupLink = new AjaxLink("openPopup") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                popupContainerProvider.setPopupContentForPage(getPage(), datasetCanvasItemPopupPanel);
                popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
                String js = "$('#" + getMarkupId() + "').popup({simetriasPatch: true, popup: $('#" + datasetCanvasItemPopupPanel.getMarkupId() + "').find('.ui.datasetPopup.popup'), on : 'click'}).popup('toggle')";
                ajaxRequestTarget.appendJavaScript(js);
            }
        };
        add(openPopupLink);
    }

    private void createDatasetCanvasItemPopupPanel() {
        datasetCanvasItemPopupPanel = new DatasetCanvasItemPopupPanel("content", datasetDescriptor, () -> {
            popupContainerProvider.refreshContainer(getPage(), getRequestCycle().find(AjaxRequestTarget.class));
            callbacks.onDelete();
        });
    }

    public DatasetCanvasItemData getData() {
        return data;
    }

    public interface Callbacks extends Serializable {

        void onDelete();

    }
}
