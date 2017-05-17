package portal.notebook.webapp.cell.visual.parallelcoordinateplot;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Created by timbo on 17/05/17.
 */
public class ParallelCoordinatePlotPanel extends Panel {

    public ParallelCoordinatePlotPanel(String id) {
        super(id);
        add(new Label("label", "Kilroy woz here!"));
    }

}
