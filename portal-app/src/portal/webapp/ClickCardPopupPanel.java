package portal.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.service.api.DatasetDescriptor;

/**
 * @author simetrias
 */
public class ClickCardPopupPanel extends Panel {

    public ClickCardPopupPanel(String id, DatasetDescriptor datasetDescriptor) {
        super(id, new CompoundPropertyModel<>(datasetDescriptor));
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        add(new Label("description"));
    }
}
