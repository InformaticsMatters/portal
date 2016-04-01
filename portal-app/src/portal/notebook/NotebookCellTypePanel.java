package portal.notebook;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.squonk.notebook.api.CellDefinition;
import portal.PortalWebApplication;

/**
 * @author simetrias
 */
public class NotebookCellTypePanel extends Panel {

    private static final String APP_RESOURCES_SUBPACKAGE = "resources";
    private final CellDefinition cellDefinition;

    public NotebookCellTypePanel(String id, CellDefinition cellDefinition) {
        super(id);
        this.cellDefinition = cellDefinition;
        addComponents();
    }

    private void addComponents() {
        Label cellName = new Label("description", cellDefinition.getDescription());
        add(cellName);
        cellName.add(new AttributeModifier("title", cellDefinition.getDescription()));
        add(new Image("cellIcon", new PackageResourceReference(PortalWebApplication.class, APP_RESOURCES_SUBPACKAGE + "/" + cellDefinition.getIcon())));
    }
}
