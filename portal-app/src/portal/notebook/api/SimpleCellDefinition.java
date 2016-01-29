package portal.notebook.api;


/**
 * temporary
 */
public class SimpleCellDefinition extends CellDefinition {


    @Override
    public CellExecutor getCellExecutor() {
        return new DummyJobCellExecutor();
    }
}
