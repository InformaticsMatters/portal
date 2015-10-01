package portal.webapp.notebook;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class TableDisplayCanvasItemPanel extends CanvasItemPanel<TableDisplayCell> {
    @Inject
    private NotebooksSession notebooksSession;

    public TableDisplayCanvasItemPanel(String id, Notebook notebook, TableDisplayCell cell) {
        super(id, notebook, cell);
    }
}
