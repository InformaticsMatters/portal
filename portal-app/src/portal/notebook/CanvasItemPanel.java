package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class CanvasItemPanel<T extends CellModel> extends Panel {
    private final T cell;

    public CanvasItemPanel(String id, T cell) {
        super(id);
        this.cell = cell;
    }

    public T getCell() {
        return cell;
    }

}
