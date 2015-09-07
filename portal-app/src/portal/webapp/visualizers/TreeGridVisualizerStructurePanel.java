package portal.webapp.visualizers;

import org.apache.wicket.markup.html.panel.Panel;
import portal.webapp.ExternalStructureImage;

import java.util.UUID;

public class TreeGridVisualizerStructurePanel extends Panel {

    public TreeGridVisualizerStructurePanel(String id, Long datasetDescriptorId, UUID rowId) {
        super(id);
        add(new ExternalStructureImage("image", rowId.toString(), datasetDescriptorId.toString(), "structureImageResource"));
    }

}
