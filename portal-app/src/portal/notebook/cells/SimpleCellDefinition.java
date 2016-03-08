package portal.notebook.cells;


import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellExecutor;
import portal.notebook.cells.DummyCellExecutor;

/**
 * temporary
 */
public class SimpleCellDefinition extends CellDefinition {

    public SimpleCellDefinition() {}

    public SimpleCellDefinition(String name, String description, String[] tags, Boolean executable) {
        super(name, description, tags, executable);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new DummyCellExecutor();
    }
}
