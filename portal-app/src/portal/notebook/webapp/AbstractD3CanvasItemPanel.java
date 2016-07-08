package portal.notebook.webapp;

import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.CellInstance;

/**
 * Created by timbo on 07/07/2016.
 */
public abstract class AbstractD3CanvasItemPanel extends CanvasItemPanel {

    public static final String OPTION_X_AXIS = "xAxis";
    public static final String OPTION_Y_AXIS = "yAxis";

    protected int svgWidth, svgHeight;

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

}
