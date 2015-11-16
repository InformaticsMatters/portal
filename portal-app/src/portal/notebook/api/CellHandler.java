package portal.notebook.api;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface CellHandler {

    void execute(String cellName);
    boolean handles(CellType cellType);
}
