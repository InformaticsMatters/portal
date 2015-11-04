package portal.notebook;

public class TableDisplayHandler implements CellHandler {
    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.TABLE_DISPLAY);
        return cell;
    }

    @Override
    public void execute(Cell cell) {

    }
}
