package portal.webapp.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class NotebookCellDescriptorPanel extends Panel {

    private final CellDescriptor cellDescriptor;

    @Inject
    private NotebooksSession notebooksSession;

    public NotebookCellDescriptorPanel(String id, CellDescriptor cellDescriptor) {
        super(id);
        this.cellDescriptor = cellDescriptor;
        addComponents();
    }

    private void addComponents() {
        add(new Label("description", cellDescriptor.getDescription()));
    }
}
