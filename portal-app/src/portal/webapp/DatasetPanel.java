package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import portal.dataset.IDatasetDescriptor;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class DatasetPanel extends Panel {

    private final IDatasetDescriptor datasetDescriptor;
    private final Callbacks callbacks;
    private DatasetPopupPanel datasetPopupPanel;

    @Inject
    private PopupContainerProvider popupContainerProvider;
    private AjaxLink openPopupLink;

    public DatasetPanel(String id, IDatasetDescriptor datasetDescriptor, Callbacks callbacks) {
        super(id);
        this.callbacks = callbacks;
        this.datasetDescriptor = datasetDescriptor;
        setOutputMarkupId(true);
        addComponents();
        createDatasetPopupPanel();
    }

    private void addComponents() {
        add(new Label("description", datasetDescriptor.getDescription()));
        add(new Label("rowCount", datasetDescriptor.getRowCount()));

        openPopupLink = new AjaxLink("openPopup") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                popupContainerProvider.setPopupContentForPage(getPage(), datasetPopupPanel);
                popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
                String js = "$('#" + getMarkupId() + "').popup({simetriasPatch: true, popup: $('#" + datasetPopupPanel.getMarkupId() + "').find('.ui.datasetPopup.popup'), on : 'click'}).popup('toggle')";
                ajaxRequestTarget.appendJavaScript(js);
            }
        };
        add(openPopupLink);
    }

    private void createDatasetPopupPanel() {
        datasetPopupPanel = new DatasetPopupPanel("content", datasetDescriptor, () -> {
            popupContainerProvider.refreshContainer(getPage(), getRequestCycle().find(AjaxRequestTarget.class));
            callbacks.onDelete();
        });
    }

    public interface Callbacks extends Serializable {

        void onDelete();

    }
}
