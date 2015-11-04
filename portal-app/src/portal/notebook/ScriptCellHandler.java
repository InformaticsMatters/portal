package portal.notebook;


public class ScriptCellHandler implements CellHandler {
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

    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.CODE);
    }
}
