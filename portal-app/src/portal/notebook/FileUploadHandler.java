package portal.notebook;

public class FileUploadHandler implements CellHandler {
    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.FILE_UPLOAD);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("file");
        variable.setVariableType(VariableType.FILE);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(Cell cell) {

    }
}
