package portal.notebook.api;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface QnxCellExecutor {

    void execute(String cellName);
    boolean handles(CellType cellType);
}
