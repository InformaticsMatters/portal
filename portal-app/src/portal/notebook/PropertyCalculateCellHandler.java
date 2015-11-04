package portal.notebook;

public class PropertyCalculateCellHandler implements CellHandler {
    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.PROPERTY_CALCULATE);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("outputFile");
        variable.setVariableType(VariableType.FILE);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(Cell cell) {

    }

    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.PROPERTY_CALCULATE);
    }

}