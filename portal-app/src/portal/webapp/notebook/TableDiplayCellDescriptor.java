package portal.webapp.notebook;

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
