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

/**
 * Created by timbo on 13/05/17.
 */
public class DefaultCellDatasetProvider implements Serializable {

    protected final Long cellId;
    protected final NotebookSession notebookSession;
    protected final String variableBindingName;
    private final String filterOptionBindingName;
    private final String selectionOptionName;

    public DefaultCellDatasetProvider(NotebookSession notebookSession, Long cellId, String variableBindingName, String filterOptionBindingName, String selectionOptionName) {
        this.notebookSession = notebookSession;
        this.cellId = cellId;
        this.variableBindingName = variableBindingName;
        this.filterOptionBindingName = filterOptionBindingName;
        this.selectionOptionName = selectionOptionName;
    }

    protected VariableInstance getBoundVariableInstance(CellInstance cellInstance) {
        return cellInstance.getBoundVariableInstance(variableBindingName);
    }

    protected VariableInstance getBoundVariableInstance() {
        return getBoundVariableInstance(getCellInstance());
    }

    public CellInstance getSourceCellInstance() {

        VariableInstance boundVariableInstance = getBoundVariableInstance();
        if (boundVariableInstance == null) {
            return null;
        }
        return notebookSession.getCurrentNotebookInstance().findCellInstanceById(boundVariableInstance.getCellId());
    }

    public Dataset getFilteredDataset() throws Exception {
        CellInstance cellInstance = getCellInstance();
        VariableInstance boundVariable = getBoundVariableInstance(cellInstance);
        if (boundVariable == null) {
            return null;
        }

        Dataset<? extends BasicObject> dataset = notebookSession.squonkDataset(boundVariable);
        if (dataset == null) {
            return null;
        }
        if (filterOptionBindingName == null) {
            return dataset;
        }

        // apply the selection filter
        Set<UUID> selectionFilter = cellInstance.readOptionBindingFilter(filterOptionBindingName);
        if (selectionFilter == null || selectionFilter.size() == 0) {
            return dataset;
        } else {
            DatasetMetadata meta = dataset.getMetadata().clone();
            meta.setSize(-1);
            final AtomicInteger counter = new AtomicInteger(0);
            Stream<? extends BasicObject> filtered = dataset.getStream()
                    .filter((o) -> selectionFilter.contains(o.getUUID()))
                    .peek((o) -> counter.incrementAndGet())
                    .onClose(() -> meta.setSize(counter.get()));

            Dataset<? extends BasicObject> result = new Dataset(meta.getType(), filtered, meta);
            return result;
        }
    }

    public Dataset getSelectedDataset() throws Exception {

        CellInstance cellInstance = getCellInstance();

        Dataset<? extends BasicObject> dataset = getFilteredDataset();
        if (dataset == null) {
            return null;
        }
        if (selectionOptionName == null) {
            return dataset;
        }

        // apply the selection filter
        Set<UUID> selectionFilter = cellInstance.readOptionFilter(selectionOptionName);
        if (selectionFilter == null || selectionFilter.size() == 0) {
            return dataset;
        } else {
            DatasetMetadata meta = dataset.getMetadata().clone();
            meta.setSize(-1);
            final AtomicInteger counter = new AtomicInteger(0);
            Stream<? extends BasicObject> filtered = dataset.getStream()
                    .filter((o) -> selectionFilter.contains(o.getUUID()))
                    .peek((o) -> counter.incrementAndGet())
                    .onClose(() -> meta.setSize(counter.get()));

            Dataset<? extends BasicObject> result = new Dataset(meta.getType(), filtered, meta);
            return result;
        }

    }


    public DatasetMetadata getFilteredMetadata() throws Exception {

        VariableInstance boundVariable = getBoundVariableInstance();
        if (boundVariable == null) {
            return null;
        }

        DatasetMetadata<? extends BasicObject> meta = notebookSession.squonkDatasetMetadata(boundVariable);
        if (meta == null) {
            return null;
        }
        DatasetMetadata<? extends BasicObject> clone = meta.clone();
        clone.setSize(-1);
        return clone;
    }

    public DatasetMetadata getSelectedMetadata() throws Exception {

        DatasetMetadata meta = getFilteredMetadata();
        if (meta == null) {
            return null;
        }
        if (selectionOptionName == null) {
            return meta;
        }

        // get the selection filter because we need to know its size
        CellInstance cellInstance = getCellInstance();
        Set<UUID> selectionFilter = cellInstance.readOptionFilter(selectionOptionName);

        DatasetMetadata clone = meta.clone();
        clone.setSize(selectionFilter == null ? meta.getSize() : selectionFilter.size());
        return clone;
    }

    public StructureIOClient getStructureIOClient() {
        return notebookSession.getStructureIOClient();
    }

    public CellInstance getCellInstance() {
        return notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
    }


    public void saveNotebook() {
        try {
            notebookSession.storeCurrentEditable();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
