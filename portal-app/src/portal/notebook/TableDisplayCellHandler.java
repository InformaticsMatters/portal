package portal.notebook;

public class TableDisplayCellHandler implements CellHandler {
    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.TABLE_DISPLAY);
        return cell;
    }

    @Override
    public void execute(Cell cell) {

    }

    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.TABLE_DISPLAY);
    }
}
