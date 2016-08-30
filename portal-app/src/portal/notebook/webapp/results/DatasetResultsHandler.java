package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.panel.Panel;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.webapp.NotebookSession;

/**
 * Created by timbo on 28/08/2016.
 */
public class DatasetResultsHandler implements ResultsHandler {

    private final String variableName;
    private final NotebookSession notebookSession;
    private final CellInstance cellInstance;
    private final DatasetDetailsPanel panel;

    public DatasetResultsHandler(String variableName, NotebookSession notebookSession, CellInstance cellInstance) {
        this.variableName = variableName;
        this.notebookSession = notebookSession;
        this.cellInstance = cellInstance;
        panel = new DatasetDetailsPanel("viewer");
    }

    public String getVariableName() {
        return variableName;
    }

    public Panel getPanel() {
        return panel;
    }

    public boolean preparePanelForDisplay() throws Exception {

        DatasetMetadata meta = fetchOutputMetadata();
        panel.setDatasetMetadata(meta);
        return meta != null;

    }

    public String getExtraJavascriptForResultsViewer() {
        return "$('#:modalElement .menu .item').tab();\n$('#:modalElement .ui.accordion').accordion();\n";
    }

    private DatasetMetadata fetchOutputMetadata() throws Exception {

        VariableInstance variableInstance = cellInstance.getVariableInstanceMap().get(variableName);
        DatasetMetadata<? extends BasicObject> meta = null;
        if (variableInstance != null) {
            meta = notebookSession.squonkDatasetMetadata(variableInstance);
        }
        return meta;
    }


}
