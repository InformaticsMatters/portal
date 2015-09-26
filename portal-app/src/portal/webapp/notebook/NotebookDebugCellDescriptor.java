package portal.webapp.notebook;

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
