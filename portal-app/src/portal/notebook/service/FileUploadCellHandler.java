package portal.notebook.service;

import portal.notebook.api.CellType;
import portal.notebook.api.VariableType;

public class FileUploadCellHandler implements CellHandler {
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
    public void execute(String cellName) {

    }

    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.FILE_UPLOAD);
    }
}
