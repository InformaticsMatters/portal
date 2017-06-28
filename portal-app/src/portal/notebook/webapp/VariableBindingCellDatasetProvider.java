package portal.notebook.webapp;

import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/** CellDatasetProvider that provides its Dataset from a bound variable and optionally filters the data based on a selection.
 * Created by timbo on 13/05/17.
 */
public class VariableBindingCellDatasetProvider extends AbstractCellDatasetProvider {

    protected final String variableBindingName;
    private final String filterOptionBindingName;
    private final String selectionOptionName;

    public VariableBindingCellDatasetProvider(NotebookSession notebookSession, Long cellId, String variableBindingName, String filterOptionBindingName, String selectionOptionName) {
        super(notebookSession, cellId);
        this.variableBindingName = variableBindingName;
        this.filterOptionBindingName = filterOptionBindingName;
        this.selectionOptionName = selectionOptionName;
    }

    @Override
    protected VariableInstance getVariableInstance() {
        BindingInstance bindingInstance = getCellInstance().getBindingInstance(variableBindingName);
        return bindingInstance == null ? null : bindingInstance.getVariableInstance();
    }

    @Override
    public Dataset getInputDataset() throws Exception {
        VariableInstance boundVariable = getVariableInstance();
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
        Set<UUID> selectionFilter = getCellInstance().readOptionBindingFilter(filterOptionBindingName);
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

            Dataset<? extends BasicObject> result = new Dataset(filtered, meta);
            return result;
        }
    }

    @Override
    public Dataset getSelectedDataset() throws Exception {

        Dataset<? extends BasicObject> dataset = getInputDataset();
        if (dataset == null) {
            return null;
        }
        if (selectionOptionName == null) {
            return dataset;
        }

        // apply the selection filter
        Set<UUID> selectionFilter = getCellInstance().readOptionFilter(selectionOptionName);
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

            Dataset<? extends BasicObject> result = new Dataset(filtered, meta);
            return result;
        }
    }

    @Override
    public DatasetMetadata getInputMetadata() throws Exception {

        VariableInstance boundVariable = getVariableInstance();
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

    @Override
    public DatasetMetadata getSelectedMetadata() throws Exception {

        DatasetMetadata meta = getInputMetadata();
        if (meta == null) {
            return null;
        }
        if (selectionOptionName == null) {
            return meta;
        }

        // get the selection filter because we need to know its size
        Set<UUID> selectionFilter = getCellInstance().readOptionFilter(selectionOptionName);

        DatasetMetadata clone = meta.clone();
        clone.setSize(selectionFilter == null ? meta.getSize() : selectionFilter.size());
        return clone;
    }

}
