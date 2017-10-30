package portal.notebook.webapp;

import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

import java.io.Serializable;

/**
 * Created by timbo on 18/10/17.
 */
public abstract class AbstractCellVariableProvider implements Serializable {

    protected final NotebookSession notebookSession;
    protected final Long cellId;

    public AbstractCellVariableProvider(NotebookSession notebookSession, Long cellId) {
        this.notebookSession = notebookSession;
        this.cellId = cellId;
    }

    /** The actual cell that is involved
     *
     * @return
     */
    public CellInstance getCellInstance() {
        return notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
    }

    public abstract VariableInstance getVariableInstance();

    public <T> T readVariable(Class<T> type) throws Exception {
        VariableInstance variableInstance = getVariableInstance();
        if (variableInstance != null) {
            return notebookSession.readStreamValueAs(variableInstance, type);
        }
        return null;
    }
}
