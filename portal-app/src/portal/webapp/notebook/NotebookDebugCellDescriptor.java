package portal.webapp.notebook;

import portal.notebook.CellDescriptor;
import portal.notebook.CellType;

public class NotebookDebugCellDescriptor implements CellDescriptor {

    @Override
    public CellType getCellType() {
        return CellType.NOTEBOOK_DEBUG;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Debug cell";
    }
}
