package portal.notebook;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.Serializable;

@ApplicationScoped
public class CellHandlerProvider implements Serializable {
    @Inject
    private Instance<CellHandler> cellHandlerInstance;

    public CellHandler getCellHandler(CellType cellType) {

        for (CellHandler cellHandler : cellHandlerInstance) {
            if (cellHandler.handles(cellType)) {
                return cellHandler;
            }
        }
        return null;
    }



}
