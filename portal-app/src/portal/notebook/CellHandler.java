package portal.notebook;

public interface CellHandler {

    Cell createCell();
    void execute(Cell cell);
}
