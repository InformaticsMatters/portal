package portal.webapp.notebook;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookCellsPanel extends Panel {

    private WebMarkupContainer cellsContainer;
    private ListView<CellDescriptor> cellRepeater;

    @Inject
    private NotebooksSession notebooksSession;

    public NotebookCellsPanel(String id) {
        super(id);
        addCells();
    }

    private void addCells() {
        cellsContainer = new WebMarkupContainer("cellsContainer");
        cellsContainer.setOutputMarkupId(true);

        List<CellDescriptor> cells = notebooksSession.listCellDescriptor();
        cellRepeater = new ListView<CellDescriptor>("cell", cells) {

            @Override
            protected void populateItem(ListItem<CellDescriptor> listItem) {
                // add cell attributes
            }
        };
        cellsContainer.add(cellRepeater);

        add(cellsContainer);
    }

}
