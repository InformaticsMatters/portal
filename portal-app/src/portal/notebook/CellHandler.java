package portal.notebook;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface CellHandler {

    Cell createCell();
    void execute(Long notebookId, String cellName);

    boolean handles(CellType cellType);
}
