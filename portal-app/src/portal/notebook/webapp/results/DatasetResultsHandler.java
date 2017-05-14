package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.webapp.DefaultCellDatasetProvider;
import portal.notebook.webapp.NotebookSession;

/**
 * Created by timbo on 28/08/2016.
 */
public class DatasetResultsHandler extends DefaultResultsHandler {

    private final DefaultCellDatasetProvider cellDatasetProvider;
    private DatasetDetailsPanel panel;

    public DatasetResultsHandler(String variableName, NotebookSession notebookSession, Long cellId) {
        super(variableName, notebookSession, cellId);
        this.cellDatasetProvider = new DefaultCellDatasetProvider(notebookSession, cellId, variableName, null, null);
    }

    public DatasetResultsHandler(String variableName, NotebookSession notebookSession, Long cellId, DefaultCellDatasetProvider cellDatasetProvider) {
        super(variableName, notebookSession, cellId);
        this.cellDatasetProvider = cellDatasetProvider;
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
        return getPanelImpl().prepare(cellDatasetProvider.getSelectedMetadata());
    }

    public String getExtraJavascriptForResultsViewer() {
        return "$('#:modalElement .menu .item').tab();\n$('#:modalElement .ui.accordion').accordion();\n";
    }


}
