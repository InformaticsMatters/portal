package portal.notebook;

import java.io.Serializable;

/**
 * @author simetrias
 */
public interface CellCallbackHandler extends Serializable {

    void onRemove(CellModel cellModel);

    void onEditBindings(CellModel cellModel);

    void onContentChanged();
}
