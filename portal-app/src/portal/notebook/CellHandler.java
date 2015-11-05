package portal.notebook;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface CellHandler {

    Cell createCell();
    void execute(Notebook notebook, Cell cell);

    boolean handles(CellType cellType);
}
