package portal.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class FolderPanel extends Panel {


    public FolderPanel(String id) {
        super(id);
        setOutputMarkupId(true);
        addComponents();
    }

    private void addComponents() {
        add(new Label("description"));
    }

}
