package portal.notebook.webapp;

import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.webapp.AbstractCellVariableProvider;
import portal.notebook.webapp.NotebookSession;

/**
 * Created by timbo on 18/10/17.
 */
public class CellOutputVariableProvider extends AbstractCellVariableProvider {

    private final String variableName;

    public CellOutputVariableProvider(NotebookSession notebookSession, Long cellId, String variableName) {
        super(notebookSession, cellId);
        this.variableName = variableName;
    }

    @Override
    public VariableInstance getVariableInstance() {
        CellInstance sourceCell = getCellInstance();
        return sourceCell == null ? null : sourceCell.getVariableInstance(variableName);
    }

}
