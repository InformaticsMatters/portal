package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import toolkit.wicket.semantic.SemanticModalPanel;

/**
 * Created by timbo on 27/08/2016.
 */
public class ResultsViewerPanel extends SemanticModalPanel {

    private ResultsHandler resultsHandler;

    public ResultsViewerPanel(String id, String modalElementWicketId, ResultsHandler resultsHandler) {
        super(id, modalElementWicketId);
        this.resultsHandler = resultsHandler;
        addContent();
    }

    private void addContent() {

        getModalRootComponent().add(resultsHandler.getPanel());

        getModalRootComponent().add(new Label("cellName", resultsHandler.getCellInstance().getName()));
        getModalRootComponent().add(new Label("variableName", resultsHandler.getVariableName()));
    }
}