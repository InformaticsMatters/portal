package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import portal.dataset.IDatasetDescriptor;

/**
 * @author simetrias
 */
public class ChemcentralDatasetPopupPanel extends Panel {

    public ChemcentralDatasetPopupPanel(String id) {
        super(id);
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        add(new Label("description"));
        add(new Label("rowCount"));

        add(new IndicatingAjaxLink("open") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                IDatasetDescriptor datasetDescriptor = (IDatasetDescriptor) ChemcentralDatasetPopupPanel.this.getDefaultModelObject();
                TreeGridVisualizerPage page = new TreeGridVisualizerPage(datasetDescriptor);
                setResponsePage(page);
            }
        });
    }
}
