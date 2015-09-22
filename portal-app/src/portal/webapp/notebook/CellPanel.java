package portal.webapp.notebook;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class CellPanel<T extends Cell> extends Panel {
    private final T cellDescriptor;
    private final Notebook notebook;

    public CellPanel(String id, Notebook notebook, T cellDescriptor) {
        super(id);
        this.notebook = notebook;
        this.cellDescriptor = cellDescriptor;
    }

    public T getCellDescriptor() {
        return cellDescriptor;
    }

    public Notebook getNotebook() {
        return notebook;
    }
}
