package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.webapp.NotebookSession;

/**
 * Created by timbo on 01/05/17.
 */
public class NullResultsHandler extends DefaultResultsHandler {


    public NullResultsHandler(String variableName, NotebookSession notebookSession, Long cellId) {
        super(variableName, notebookSession, cellId);
    }


    @Override
    public Panel getPanel() {
        return new NoResultsPanel("resultsViewer", "modalElement");
    }

    @Override
    public boolean preparePanelForDisplay() throws Exception {
        return false;
    }

    @Override
    public String getExtraJavascriptForResultsViewer() {
        return null;
    }
}
