package portal.webapp.notebook;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class CellPanel<T extends CellDescriptor> extends Panel {
    private final T cellDescriptor;
    private final NotebookDescriptor notebookDescriptor;

    public CellPanel(String id, NotebookDescriptor notebookDescriptor, T cellDescriptor) {
        super(id);
        this.notebookDescriptor = notebookDescriptor;
        this.cellDescriptor = cellDescriptor;
    }

    public T getCellDescriptor() {
        return cellDescriptor;
    }

    public NotebookDescriptor getNotebookDescriptor() {
        return notebookDescriptor;
    }
}
