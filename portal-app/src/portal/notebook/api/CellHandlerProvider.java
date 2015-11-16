package portal.notebook.api;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.Serializable;

public class CellHandlerProvider implements Serializable {
    @Inject
    private Instance<CellHandler> cellHandlerInstance;

    public CellHandler resolveCellHandler(CellType cellType) {

        for (CellHandler cellHandler : cellHandlerInstance) {
            if (cellHandler.handles(cellType)) {
                return cellHandler;
            }
        }
        return null;
    }

}
