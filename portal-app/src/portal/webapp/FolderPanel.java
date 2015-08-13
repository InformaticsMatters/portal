package portal.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class FolderPanel extends Panel {

    private final String folderDescription;

    public FolderPanel(String id, String folderDescription) {
        super(id);
        this.folderDescription = folderDescription;
        setOutputMarkupId(true);
        addComponents();
    }

    private void addComponents() {
        add(new Label("description", folderDescription));
    }

}
