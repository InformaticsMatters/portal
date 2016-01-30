package portal.notebook.cells;


import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellExecutor;
import portal.notebook.cells.DummyCellExecutor;

/**
 * temporary
 */
public class SimpleCellDefinition extends CellDefinition {


    @Override
    public CellExecutor getCellExecutor() {
        return new DummyCellExecutor();
    }
}
