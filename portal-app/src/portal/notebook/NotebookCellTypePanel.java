package portal.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.PackageResourceReference;
import portal.PortalWebApplication;
import portal.notebook.api.CellDefinition;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class NotebookCellTypePanel extends Panel {

    private final CellDefinition cellType;

    @Inject
    private NotebookSession notebookSession;

    public NotebookCellTypePanel(String id, CellDefinition cellType) {
        super(id);
        this.cellType = cellType;
        addComponents();
    }

    private void addComponents() {

        add(new Label("description", cellType.getDescription()));

        add(new Image("cellIcon", new PackageResourceReference(PortalWebApplication.class, "resources/img2.png")));
    }
}
