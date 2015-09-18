package portal.webapp.notebook;

public class NotebookDebugCellTemplate implements CellTemplate<NotebookDebugCellDescriptor> {

    @Override
    public CellType getCellType() {
        return CellType.DEBUG;
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
