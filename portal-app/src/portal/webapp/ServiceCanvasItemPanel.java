package portal.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPanel extends Panel {

    public ServiceCanvasItemPanel(String id, ServiceCanvasItemData model) {
        super(id);
        setOutputMarkupId(true);

        add(new Label("id", model.getServiceDescriptor().getId()));
        add(new Label("name", model.getServiceDescriptor().getName()));
    }
}
