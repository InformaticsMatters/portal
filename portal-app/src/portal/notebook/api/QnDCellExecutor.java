package portal.notebook.api;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface QnDCellExecutor {

    void execute(String cellName);
    boolean handles(CellType cellType);
}
