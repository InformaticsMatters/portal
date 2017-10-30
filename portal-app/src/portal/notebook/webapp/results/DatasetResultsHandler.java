package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.panel.Panel;
import org.squonk.dataset.DatasetMetadata;
import portal.notebook.webapp.AbstractCellDatasetProvider;
import portal.notebook.webapp.CanvasItemPanel;
import portal.notebook.webapp.NotebookSession;

import java.util.ArrayList;

/**
 * Created by timbo on 28/08/2016.
 */
public class DatasetResultsHandler extends DefaultResultsHandler {

    private final AbstractCellDatasetProvider cellDatasetProvider;
    private DatasetDetailsPanel panel;

    public DatasetResultsHandler(String variableName, NotebookSession notebookSession, CanvasItemPanel sourcePanel, AbstractCellDatasetProvider cellDatasetProvider) {
        super(variableName, notebookSession, sourcePanel);
        this.cellDatasetProvider = cellDatasetProvider;
    }

    @Override
    public Panel getPanel() {
        if (panel == null) {
            panel = new DatasetDetailsPanel("viewer", cellDatasetProvider, sourcePanel.collectExpandedPanels(new ArrayList<>()));
        }
        return panel;
    }

    private DatasetDetailsPanel getPanelImpl() {
        return (DatasetDetailsPanel) getPanel();
    }

    public boolean preparePanelForDisplay() throws Exception {
        DatasetDetailsPanel panel = getPanelImpl();
        DatasetMetadata meta = cellDatasetProvider.getSelectedMetadata();
        return panel.prepare(meta);
    }

    public String getExtraJavascriptForResultsViewer() {
        return "$('#:modalElement .menu .item').tab();\n$('#:modalElement .ui.accordion').accordion();\n";
    }


}
