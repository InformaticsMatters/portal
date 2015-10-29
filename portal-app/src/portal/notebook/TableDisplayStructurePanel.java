package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;

import java.util.UUID;

public class TableDisplayStructurePanel extends Panel {

    public TableDisplayStructurePanel(String id, Long datasetDescriptorId, UUID rowId) {
        super(id);
        add(new NotebookStructureImage("image", rowId.toString(), datasetDescriptorId.toString(), "notebookStructureImageResource"));
    }

}
