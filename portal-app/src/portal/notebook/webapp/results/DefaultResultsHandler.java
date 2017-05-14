package portal.notebook.webapp.results;

import portal.notebook.api.CellInstance;
import portal.notebook.webapp.CanvasItemPanel;
import portal.notebook.webapp.NotebookSession;

/**
 * Created by timbo on 01/05/17.
 */
public abstract class DefaultResultsHandler implements ResultsHandler {

    protected final String variableName;
    protected final NotebookSession notebookSession;
    protected final CanvasItemPanel sourcePanel;
    protected final Long cellId;

    public DefaultResultsHandler(String variableName, NotebookSession notebookSession, CanvasItemPanel sourcePanel) {
        this.variableName = variableName;
        this.notebookSession = notebookSession;
        this.sourcePanel = sourcePanel;
        this.cellId = sourcePanel.getCellId();
    }

    @Override
    public String getVariableName() {
        return null;
    }

    @Override
    public CellInstance getCellInstance() {
        return notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
    }
}
