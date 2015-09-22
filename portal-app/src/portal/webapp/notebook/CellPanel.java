package portal.webapp.notebook;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class CellPanel<T extends Cell> extends Panel {
    private final T cell;
    private final Notebook notebook;

    public CellPanel(String id, Notebook notebook, T cell) {
        super(id);
        this.notebook = notebook;
        this.cell = cell;
    }

    public T getCell() {
        return cell;
    }

    public Notebook getNotebook() {
        return notebook;
    }
}
