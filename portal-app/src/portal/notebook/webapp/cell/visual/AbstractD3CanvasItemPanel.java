package portal.notebook.webapp.cell.visual;

import org.squonk.dataset.DatasetSelection;
import org.squonk.types.io.JsonHandler;
import portal.notebook.webapp.AbstractDatasetAdvancedOptionsPanel;
import portal.notebook.webapp.CanvasItemPanel;
import portal.notebook.webapp.NotebookSession;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by timbo on 07/07/2016.
 */
public abstract class AbstractD3CanvasItemPanel extends CanvasItemPanel {

    public static final String OPTION_X_AXIS = "xAxis";
    public static final String OPTION_Y_AXIS = "yAxis";
    public static final String OPTION_EXTENTS ="extents";
    public static final String OPTION_AXES ="axes";
    public static final String OPTION_FIELDS = "fields";

    private static final Logger LOG = Logger.getLogger(AbstractD3CanvasItemPanel.class.getName());

    public AbstractD3CanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
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

    protected DatasetSelection readSelectionJson(String json) {
        if (json != null) {
            try {
                Stream<UUID> stream = JsonHandler.getInstance().streamFromJson(json, UUID.class);
                List<UUID> list = stream.collect(Collectors.toList());
                //if (list.size() > 0) {
                    return new DatasetSelection(list);
                //}
            } catch (Exception e) {
                notifyMessage("Error", "Invalid selection");
                LOG.log(Level.WARNING, "Invalid selection", e);
            }
        }
        return null;
    }

    public abstract class DefaultCallbackHandler implements AbstractDatasetAdvancedOptionsPanel.CallbackHandler {


        @Override
        public void notifyMessage(String title, String message) {
            AbstractD3CanvasItemPanel.this.notifyMessage(title, message);
        }

        @Override
        public NotebookSession getNotebookSesion() {
            return AbstractD3CanvasItemPanel.this.getNotebookSession();
        }

    }

}
