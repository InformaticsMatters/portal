package portal.notebook;

import chemaxon.nfunk.jep.function.Not;

public class TableDisplayCellHandler implements CellHandler {
    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.TABLE_DISPLAY);
        return cell;
    }

    @Override
    public void execute(Long notebookId, String cellName) {

    }

    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.TABLE_DISPLAY);
    }
}
