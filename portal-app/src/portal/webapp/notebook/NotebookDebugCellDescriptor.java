package portal.webapp.notebook;


public class NotebookDebugCellDescriptor implements CellDescriptor {
    @Override
    public Class getCellClass() {
        return NotebookDebugCellPanel.class;
    }
}
