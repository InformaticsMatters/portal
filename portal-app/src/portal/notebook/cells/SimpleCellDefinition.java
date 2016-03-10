package portal.notebook.cells;


import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellExecutor;

/**
 * temporary
 */
public class SimpleCellDefinition extends CellDefinition {
    private final static long serialVersionUID = 1l;

    public SimpleCellDefinition() {}

    public SimpleCellDefinition(String name, String description, String[] tags, Boolean executable) {
        super(name, description, tags, executable);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new DummyCellExecutor();
    }
}
