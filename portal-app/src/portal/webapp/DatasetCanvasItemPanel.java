package portal.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import portal.integration.DatamartSession;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class DatasetCanvasItemPanel extends Panel {

    private final DatasetCanvasItemModel model;
    @Inject
    private DatamartSession datamartSession;

    public DatasetCanvasItemPanel(String id, DatasetCanvasItemModel model) {
        super(id);
        this.model = model;
        setOutputMarkupId(true);
        add(new Label("id", model.getId()));
        addItem();
    }

    private void addItem() {

        Label descriptionLabel = new Label("description", datasetDescriptor.getDescription());
        add(descriptionLabel);

    }

}
