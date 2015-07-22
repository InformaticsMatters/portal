package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class DatasetCanvasItemPanel extends Panel {

    private final DatasetCanvasItemData data;

    public DatasetCanvasItemPanel(String id, DatasetCanvasItemData data) {
        super(id);
        this.data = data;
        setOutputMarkupId(true);

        add(new Label("id", data.getDatasetDescriptor().getId()));
        add(new Label("description", data.getDatasetDescriptor().getDescription()));
        add(new Label("rowCount", data.getDatasetDescriptor().getRowCount()));
        add(new IndicatingAjaxLink("open") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                TreeGridVisualizerPage page = new TreeGridVisualizerPage(data.getDatasetDescriptor());
                setResponsePage(page);
            }
        });

    }

    public DatasetCanvasItemData getData() {
        return data;
    }
}
