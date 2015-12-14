package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;

public abstract class CanvasItemPanel<T extends CellModel> extends Panel {
    private transient final T cellModel;
    private final CallbackHandler callbackHandler;

    public CanvasItemPanel(String id, T cellModel, CallbackHandler callbackHandler) {
        super(id);
        this.cellModel = cellModel;
        this.callbackHandler = callbackHandler;
    }

    public T getCellModel() {
        return cellModel;
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public interface CallbackHandler extends Serializable {
        void onRemove(CellModel cellModel);

        void onEditBindings(String markupId, CellModel cellModel);
    }

}
