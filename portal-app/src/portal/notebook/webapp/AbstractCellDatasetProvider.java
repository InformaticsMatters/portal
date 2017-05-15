package portal.notebook.webapp;

import org.squonk.core.client.StructureIOClient;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/** Base class for Dataset provider.
 * Created by timbo on 13/05/17.
 */
public abstract class AbstractCellDatasetProvider implements Serializable {

    protected final NotebookSession notebookSession;
    protected final Long cellId;

    public AbstractCellDatasetProvider(NotebookSession notebookSession, Long cellId) {
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

    protected abstract VariableInstance getVariableInstance();

    public abstract Dataset getInputDataset() throws Exception;

    public abstract Dataset getSelectedDataset() throws Exception;

    public abstract DatasetMetadata getInputMetadata() throws Exception;

    public abstract DatasetMetadata getSelectedMetadata() throws Exception;

    public StructureIOClient getStructureIOClient() {
        return notebookSession.getStructureIOClient();
    }

    public void saveNotebook() {
        try {
            notebookSession.storeCurrentEditable();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
