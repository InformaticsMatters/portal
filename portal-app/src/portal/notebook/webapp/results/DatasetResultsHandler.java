package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.panel.Panel;
import org.squonk.core.client.StructureIOClient;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.DatasetProvider;
import org.squonk.types.BasicObject;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.webapp.NotebookSession;

import java.io.Serializable;

/**
 * Created by timbo on 28/08/2016.
 */
public class DatasetResultsHandler implements ResultsHandler {

    private final String variableName;
    private final NotebookSession notebookSession;
    private final Long cellId;
    private final CellDatasetProvider cellDatasetProvider;
    private DatasetDetailsPanel panel;

    public DatasetResultsHandler(String variableName, NotebookSession notebookSession, StructureIOClient structureIOClient, Long cellId) {
        this.variableName = variableName;
        this.notebookSession = notebookSession;
        this.cellId = cellId;
        this.cellDatasetProvider = new CellDatasetProvider(cellId, notebookSession, structureIOClient, variableName);
    }

    @Override
    public String getVariableName() {
        return variableName;
    }

    @Override
    public CellInstance getCellInstance() {
        return notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
    }

    @Override
    public Panel getPanel() {
        if (panel == null) {
            panel = new DatasetDetailsPanel("viewer", cellDatasetProvider);
        }
        return panel;
    }

    private DatasetDetailsPanel getPanelImpl() {
        return (DatasetDetailsPanel) getPanel();
    }

    public boolean preparePanelForDisplay() throws Exception {
        return getPanelImpl().prepare(cellDatasetProvider.getMetadata());
    }

    public String getExtraJavascriptForResultsViewer() {
        return "$('#:modalElement .menu .item').tab();\n$('#:modalElement .ui.accordion').accordion();\n";
    }

    static class CellDatasetProvider implements DatasetProvider, Serializable {

        private final Long cellId;
        private final NotebookSession notebookSession;
        private final StructureIOClient structureIOClient;
        private final String variable;

        CellDatasetProvider(Long cellId, NotebookSession session, StructureIOClient structureIOClient, String variable) {
            this.cellId = cellId;
            this.notebookSession = session;
            this.structureIOClient = structureIOClient;
            this.variable = variable;
        }

        @Override
        public Dataset getDataset() throws Exception {
            VariableInstance variableInstance = getCellInstance().getVariableInstanceMap().get(variable);
            Dataset<? extends BasicObject> data = null;
            if (variableInstance != null) {
                data = notebookSession.squonkDataset(variableInstance);
            }
            return data;
        }

        @Override
        public DatasetMetadata getMetadata() throws Exception {

            VariableInstance variableInstance = getCellInstance().getVariableInstanceMap().get(variable);
            DatasetMetadata<? extends BasicObject> meta = null;
            if (variableInstance != null) {
                meta = notebookSession.squonkDatasetMetadata(variableInstance);
            }
            return meta;
        }

        public StructureIOClient getStructureIOClient() {
            return structureIOClient;
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


}
