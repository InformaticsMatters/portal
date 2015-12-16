package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;

public abstract class CanvasItemPanel<T extends CellModel> extends Panel {
    private transient final T cellModel;
    private final CallbackHandler callbackHandler;

    public CanvasItemPanel(String id, T cellModel, CallbackHandler callbackHandler) {
        super(id);
        this.cellModel = cellModel;
        this.callbackHandler = callbackHandler;
        addHeader();
    }

    private void addHeader() {
        add(new Label("cellName", getCellModel().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getCallbackHandler().onRemove(getCellModel());
            }
        });
        add(new AjaxLink("bindings") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getCallbackHandler().onEditBindings(getCellModel());
            }
        });
    }


    public T getCellModel() {
        return cellModel;
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public interface CallbackHandler extends Serializable {
        void onRemove(CellModel cellModel);

        void onEditBindings(CellModel cellModel);

        void onContentChanged();
    }

}
