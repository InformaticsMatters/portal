package portal.notebook;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface CellHandler {

    Cell createCell();
    void execute(Cell cell);

    boolean handles(CellType cellType);
}
