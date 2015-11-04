package portal.notebook;


public class ScriptHandler implements CellHandler {
    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.CODE);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("outcome");
        variable.setVariableType(VariableType.VALUE);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(Cell cell) {

    }
}
