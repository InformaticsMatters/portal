package portal.webapp.notebook;

import portal.notebook.CellDescriptor;
import portal.notebook.CellType;

public class TableDiplayCellDescriptor implements CellDescriptor {
    @Override
    public CellType getCellType() {
        return CellType.TABLE_DISPLAY;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Table display";
    }
}
