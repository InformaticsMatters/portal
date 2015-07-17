package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import portal.dataset.IDatasetDescriptor;

/**
 * @author simetrias
 */
public class DatasetPanel extends Panel {

    private final IDatasetDescriptor datasetDescriptor;
    private DatasetPopupPanel datasetPopupPanel;

    public DatasetPanel(String id, IDatasetDescriptor datasetDescriptor) {
        super(id);
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
                System.out.println("Click");
                datasetPopupPanel.setVisible(true);
                ajaxRequestTarget.add(datasetPopupPanel);
                String js = "$('#" + getMarkupId() + "').popup({popup: $('#" + DatasetPanel.this.getMarkupId() + "').find('.ui.datasetPopup.popup'), on : 'click'}).popup('toggle')";
                ajaxRequestTarget.appendJavaScript(js);
            }
        });
    }

    private void addDatasetPopupPanel() {
        datasetPopupPanel = new DatasetPopupPanel("popupPanel");
        datasetPopupPanel.setVisible(false);
        add(datasetPopupPanel);
    }
}
