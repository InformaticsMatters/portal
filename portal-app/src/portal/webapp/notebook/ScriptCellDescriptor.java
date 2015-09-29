package portal.webapp.notebook;

public class ScriptCellDescriptor implements CellDescriptor {

    @Override
    public CellType getCellType() {
        return CellType.CODE;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Code cell";
    }
}
