package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class CanvasItemPanel<T extends CellModel> extends Panel {
    private transient final T cellModel;

    public CanvasItemPanel(String id, T cellModel) {
        super(id);
        this.cellModel = cellModel;
    }

    public T getCellModel() {
        return cellModel;
    }

}
