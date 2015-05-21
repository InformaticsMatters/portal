package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class DatasetCanvasItemPanel extends Panel {

    private final DatasetCanvasItemData model;

    public DatasetCanvasItemPanel(String id, DatasetCanvasItemData model) {
        super(id);
        this.model = model;
        setOutputMarkupId(true);

        add(new Label("id", model.getDatasetDescriptor().getId()));
        add(new Label("description", model.getDatasetDescriptor().getDescription()));
        add(new Label("rowCount", model.getDatasetDescriptor().getRowCount()));
        add(new IndicatingAjaxLink("open") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                TreeGridVisualizerPage page = new TreeGridVisualizerPage(model.getDatasetDescriptor());
                setResponsePage(page);
            }
        });

    }
}
