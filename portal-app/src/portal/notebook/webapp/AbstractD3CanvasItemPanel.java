package portal.notebook.webapp;

import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.CellInstance;
import portal.notebook.webapp.results.DatasetDetailsPanel;
import toolkit.wicket.semantic.SemanticModalPanel;

import java.util.logging.Logger;

/**
 * Created by timbo on 07/07/2016.
 */
public abstract class AbstractD3CanvasItemPanel extends CanvasItemPanel {

    private static final Logger LOG = Logger.getLogger(AbstractD3CanvasItemPanel.class.getName());

    public static final String OPTION_X_AXIS = "xAxis";
    public static final String OPTION_Y_AXIS = "yAxis";
    public static final String OPTION_FIELDS = "fields";

    public AbstractD3CanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        //addComponents();
    }

    //protected void addComponents() {
    //    addResultsViewerPanel();
    //}

    @Override
    protected SemanticModalPanel createResultsViewerPanel() {
        return new DatasetDetailsPanel("datasetDetails", "modalElement");
    }

    protected Float safeConvertToFloat(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else {
            try {
                return new Float(o.toString());
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    protected Double safeConvertToDouble(Object o) {
        //LOG.info("Convert to double" + o + " [" + (o == null ? "null" : o.getClass().getName()) + "]");
        if (o == null) {
            return null;
        }
        if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else {
            try {
                return new Double(o.toString());
            } catch (NumberFormatException nfe) {
                //LOG.info("Bad double: " + o);
                return null;
            }
        }
    }

    protected String getExtraJavascriptForResultsViewer() {
        String js = super.getExtraJavascriptForResultsViewer();
        return js += "$('#:modalElement .menu .item').tab();\n";
    }

}
