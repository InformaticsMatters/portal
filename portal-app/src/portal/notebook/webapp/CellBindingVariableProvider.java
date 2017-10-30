package portal.notebook.webapp;

import portal.notebook.api.BindingInstance;
import portal.notebook.api.VariableInstance;

/** CellDatasetProvider that provides its value from a bound variable.
 *
 */
public class CellBindingVariableProvider extends AbstractCellVariableProvider {

    protected final String variableBindingName;


    public CellBindingVariableProvider(NotebookSession notebookSession, Long cellId, String variableBindingName) {
        super(notebookSession, cellId);
        this.variableBindingName = variableBindingName;
    }

    public String getVariableBindingName() {
        return variableBindingName;
    }

    @Override
    public VariableInstance getVariableInstance() {
        BindingInstance bindingInstance = getCellInstance().getBindingInstance(variableBindingName);
        return bindingInstance == null ? null : bindingInstance.getVariableInstance();
    }

}
