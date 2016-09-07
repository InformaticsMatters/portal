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
    private final StructureIOClient structureIOClient;
    private final CellInstance cellInstance;
    private final CellDatasetProvider cellDatasetProvider;
    private DatasetDetailsPanel panel;

    public DatasetResultsHandler(String variableName, NotebookSession notebookSession, StructureIOClient structureIOClient, CellInstance cellInstance) {
        this.variableName = variableName;
        this.notebookSession = notebookSession;
        this.structureIOClient = structureIOClient;
        this.cellInstance = cellInstance;
        this.cellDatasetProvider = new CellDatasetProvider(cellInstance, notebookSession, structureIOClient, variableName);
    }

    @Override
    public String getVariableName() {
        return variableName;
    }

    @Override
    public CellInstance getCellInstance() {
        return cellInstance;
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
        Dataset dataset = cellDatasetProvider.getDataset();
        return getPanelImpl().prepare(dataset);
    }

    public String getExtraJavascriptForResultsViewer() {
        return "$('#:modalElement .menu .item').tab();\n$('#:modalElement .ui.accordion').accordion();\n";
    }

    static class CellDatasetProvider implements DatasetProvider, Serializable {

        private final CellInstance cell;
        private final NotebookSession session;
        private final StructureIOClient structureIOClient;
        private final String variable;

        CellDatasetProvider(CellInstance cell, NotebookSession session, StructureIOClient structureIOClient, String variable) {
            this.cell = cell;
            this.session = session;
            this.structureIOClient = structureIOClient;
            this.variable = variable;
        }

        @Override
        public Dataset getDataset() throws Exception {
            VariableInstance variableInstance = cell.getVariableInstanceMap().get(variable);
            Dataset<? extends BasicObject> data = null;
            if (variableInstance != null) {
                data = session.squonkDataset(variableInstance);
            }
            return data;
        }

        @Override
        public DatasetMetadata getMetadata() throws Exception {

            VariableInstance variableInstance = cell.getVariableInstanceMap().get(variable);
            DatasetMetadata<? extends BasicObject> meta = null;
            if (variableInstance != null) {
                meta = session.squonkDatasetMetadata(variableInstance);
            }
            return meta;
        }

        public StructureIOClient getStructureIOClient() {
            return structureIOClient;
        }

        public CellInstance getCellInstance() {
            return cell;
        }
    }


}
