package portal.notebook;

import java.io.Serializable;

public interface NotebookChangeListener extends Serializable {

    void onCellRemoved(CellModel cellModel);
    void onCellAdded(CellModel cellModel);

}
