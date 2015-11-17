package portal.notebook.api;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.Serializable;

public class QndCellExecutorProvider implements Serializable {
    @Inject
    private Instance<QnDCellExecutor> cellHandlerInstance;

    public QnDCellExecutor resolveCellHandler(CellType cellType) {

        for (QnDCellExecutor qnDCellExecutor : cellHandlerInstance) {
            if (qnDCellExecutor.handles(cellType)) {
                return qnDCellExecutor;
            }
        }
        return null;
    }

}
