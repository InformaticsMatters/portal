package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.CellInstance;

import java.io.Serializable;

/**
 * Created by timbo on 28/08/2016.
 */
public interface ResultsHandler extends Serializable {

    String getVariableName();

    CellInstance getCellInstance();

    /** Get the Wicket panel that displays the results
     *
     * @return
     */
    Panel getPanel();

    /** The panel is about to be displayed so prepare it with any data that it needs.
     *
     * @return true if there are results to show
     * @throws Exception
     */
    boolean preparePanelForDisplay() throws Exception;

    /**
     * Generate any extra JavaScript that is needed for the results viewer to function.
     * Typically adding behaviours to DOM elements. You can include the token #:modalElement which will
     * be substituted for the ID of the outermost DIV of the modal. This allows just that modal to be targetted.
     *
     * @return
     */
    String getExtraJavascriptForResultsViewer();
}
