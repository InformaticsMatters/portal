package portal.webapp.notebook;

import java.io.Serializable;

public interface NotebookChangeListener extends Serializable {

    void onCellRemoved(Cell cell);
    void onCellAdded(Cell cell);

}
