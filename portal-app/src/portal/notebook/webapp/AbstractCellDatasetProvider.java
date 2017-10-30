package portal.notebook.webapp;

import org.squonk.core.client.StructureIOClient;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;

/** Base class for Dataset provider.
 * Created by timbo on 13/05/17.
 */
public abstract class AbstractCellDatasetProvider extends AbstractCellVariableProvider {


    public AbstractCellDatasetProvider(NotebookSession notebookSession, Long cellId) {
        super(notebookSession, cellId);
    }

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
