package portal.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.CellDefinition;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class NotebookCellTypePanel extends Panel {

    private final CellDefinition cellType;

    @Inject
    private NotebookSession notebookSession;

    public NotebookCellTypePanel(String id, CellDefinition cellType) {
        super(id);
        this.cellType = cellType;
        addComponents();
    }

    private void addComponents() {
        add(new Label("description", cellType.getDescription()));
    }
}
