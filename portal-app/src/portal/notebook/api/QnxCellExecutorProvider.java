package portal.notebook.api;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.Serializable;

public class QnxCellExecutorProvider implements Serializable {
    @Inject
    private Instance<QnxCellExecutor> cellHandlerInstance;

    public QnxCellExecutor resolveCellHandler(CellType cellType) {

        for (QnxCellExecutor qnxCellExecutor : cellHandlerInstance) {
            if (qnxCellExecutor.handles(cellType)) {
                return qnxCellExecutor;
            }
        }
        return null;
    }

}
