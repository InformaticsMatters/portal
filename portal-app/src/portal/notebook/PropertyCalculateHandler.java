package portal.notebook;

public class PropertyCalculateHandler implements CellHandler {
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

}