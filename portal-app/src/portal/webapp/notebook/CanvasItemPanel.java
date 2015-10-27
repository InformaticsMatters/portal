package portal.webapp.notebook;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class CanvasItemPanel<T extends Cell> extends Panel {
    private final T cell;
    private final NotebookData notebookData;

    public CanvasItemPanel(String id, NotebookData notebookData, T cell) {
        super(id);
        this.notebookData = notebookData;
        this.cell = cell;
    }

    public T getCell() {
        return cell;
    }

    public NotebookData getNotebookData() {
        return notebookData;
    }
}
