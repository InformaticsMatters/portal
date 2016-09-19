package portal.notebook.webapp;

import org.squonk.types.io.JsonHandler;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 07/07/2016.
 */
public abstract class AbstractD3CanvasItemPanel extends CanvasItemPanel {

    public static final String OPTION_X_AXIS = "xAxis";
    public static final String OPTION_Y_AXIS = "yAxis";
    public static final String OPTION_FIELDS = "fields";
    public static final String OPTION_SELECTED_IDS ="selectionSelected";
    public static final String OPTION_SELECTED_MARKED_IDS ="selectionSelectedMarked";
    public static final String OPTION_EXTENTS ="extents";
    public static final String OPTION_AXES ="axes";
    private static final Logger LOG = Logger.getLogger(AbstractD3CanvasItemPanel.class.getName());
    @Inject
    protected NotebookSession notebookSession;

    protected List<UUID> selectedUUIDs = new ArrayList<>();

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

    protected void readSelectionJson(String json) {
        if (json == null) {
            selectedUUIDs = null;
        } else {
            try {
                Iterator<String> iter = JsonHandler.getInstance().iteratorFromJson(json, String.class);
                List<UUID> uuids = new ArrayList<>();
                while (iter.hasNext()) {
                    uuids.add(UUID.fromString(iter.next()));
                }
                selectedUUIDs = uuids;
            } catch (Exception e) {
                notifyMessage("Error", "Invalid selection");
                LOG.log(Level.WARNING, "Invalid selection", e);
                selectedUUIDs = null;
            }
        }
    }


}
