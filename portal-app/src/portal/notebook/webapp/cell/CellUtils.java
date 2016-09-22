package portal.notebook.webapp.cell;

import org.squonk.dataset.DatasetMetadata;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.webapp.NotebookSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.poi.hslf.record.RecordTypes.List;

/**
 * Created by timbo on 22/09/2016.
 */
public class CellUtils {

    public static List<String> fieldNamesSorted(NotebookSession notebookSession, Long cellId, String variableName) throws Exception {
        List<String> fieldNames = new ArrayList<>();
        CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
        if (cellInstance != null) {
            BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(variableName);
            if (bindingInstance != null) {
                VariableInstance variableInstance = bindingInstance.getVariableInstance();
                if (variableInstance != null) {
                    DatasetMetadata datasetMetadata = notebookSession.squonkDatasetMetadata(variableInstance);
                    if (datasetMetadata != null) {
                        Set<String> items = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                        items.addAll(datasetMetadata.getValueClassMappings().keySet());
                        fieldNames.addAll(items);
                    }
                }
            }
        }
        return fieldNames;
    }
}
