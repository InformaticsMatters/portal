package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class CanvasItemPanel extends Panel {

    private final CellModel cellModel;

    public CanvasItemPanel(String id, CellModel cellModel) {
        super(id);
        this.cellModel = cellModel;
    }

    public CellModel getCellModel() {
        return cellModel;
    }

}
