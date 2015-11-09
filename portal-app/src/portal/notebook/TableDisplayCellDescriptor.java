package portal.notebook;

import portal.notebook.api.CellType;

public class TableDisplayCellDescriptor implements CellDescriptor {
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
