package portal.webapp.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;

/**
 * @author simetrias
 */

public class NotebookCellPanel extends Panel {

    private final Cell cell;

    @Inject
    private NotebooksSession notebooksSession;

    public NotebookCellPanel(String id, Cell cell) {
        super(id);
        this.cell = cell;
        addComponents();
    }

    private void addComponents() {
        add(new Label("description", cell.getName()));
    }
}
