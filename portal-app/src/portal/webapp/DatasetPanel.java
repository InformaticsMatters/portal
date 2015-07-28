package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
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

    public DatasetPanel(String id, IDatasetDescriptor datasetDescriptor, Callbacks callbacks) {
        super(id);
        this.callbacks = callbacks;
        this.datasetDescriptor = datasetDescriptor;
        setOutputMarkupId(true);
        addComponents();
        addDatasetPopupPanel();
    }

    private void addComponents() {
        add(new Label("description", datasetDescriptor.getDescription()));
        add(new Label("rowCount", datasetDescriptor.getRowCount()));

        add(new IndicatingAjaxLink("openPopup") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                popupContainerProvider.setPopupContentForPage(getPage(), datasetPopupPanel);
                popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
                String js = "$('#" + getMarkupId() + "').popup({popup: $('#" + datasetPopupPanel.getMarkupId() + "').find('.ui.datasetPopup.popup'), on : 'click'}).popup('toggle')";
                ajaxRequestTarget.appendJavaScript(js);
                /*
                datasetPopupPanel.setVisible(true);
                ajaxRequestTarget.add(datasetPopupPanel);
                String js = "$('#" + getMarkupId() + "').popup({popup: $('#" + DatasetPanel.this.getMarkupId() + "').find('.ui.datasetPopup.popup'), on : 'click'}).popup('toggle')";
                ajaxRequestTarget.appendJavaScript(js);
                */
            }
        });
    }

    private void addDatasetPopupPanel() {
        datasetPopupPanel = new DatasetPopupPanel("content", datasetDescriptor, callbacks::onDelete);
        // datasetPopupPanel = new DatasetPopupPanel("popupPanel", datasetDescriptor, callbacks::onDelete);
        // datasetPopupPanel.setVisible(false);
        // add(datasetPopupPanel);
    }

    public interface Callbacks extends Serializable {

        void onDelete();

    }
}
