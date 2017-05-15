package portal.notebook.webapp;

import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

/** CellDatasetProvider that provides its Dataset from an output variable of the specified cell
 *
 *
 * Created by timbo on 13/05/17.
 */
public class OutputVariableCellDatasetProvider extends AbstractCellDatasetProvider {

    protected final String variableName;

    public OutputVariableCellDatasetProvider(NotebookSession notebookSession, Long cellId, String variableName) {
        super(notebookSession, cellId);
        this.variableName = variableName;
    }

    @Override
    protected VariableInstance getVariableInstance() {
        CellInstance sourceCell = getCellInstance();
        return sourceCell == null ? null : sourceCell.getVariableInstance(variableName);
    }

    @Override
    public Dataset getInputDataset() throws Exception {

        VariableInstance variable = getVariableInstance();
        if (variable == null) {
            return null;
        }

        Dataset<? extends BasicObject> dataset = notebookSession.squonkDataset(variable);
        return dataset;
    }

    @Override
    public Dataset getSelectedDataset() throws Exception {

        // TODO - apply the selection filter
        return getInputDataset();
    }

    @Override
    public DatasetMetadata getInputMetadata() throws Exception {

        VariableInstance variable = getVariableInstance();
        if (variable == null) {
            return null;
        }

        DatasetMetadata<? extends BasicObject> meta = notebookSession.squonkDatasetMetadata(variable);
        return meta;
    }

    @Override
    public DatasetMetadata getSelectedMetadata() throws Exception {

        // TODO - apply the selection filter
        return getInputMetadata();
    }
}
