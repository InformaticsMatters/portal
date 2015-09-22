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
    private ListView<Cell> cellRepeater;

    @Inject
    private NotebooksSession notebooksSession;

    public NotebookCellsPanel(String id) {
        super(id);
        addCells();
    }

    private void addCells() {
        cellsContainer = new WebMarkupContainer("cellsContainer");
        cellsContainer.setOutputMarkupId(true);

        List<Cell> cells = notebooksSession.listCellDescriptor();
        cellRepeater = new ListView<Cell>("cell", cells) {

            @Override
            protected void populateItem(ListItem<Cell> listItem) {
                Cell cell = listItem.getModelObject();
                listItem.add(new NotebookCellPanel("cellItem", cell));
                listItem.setOutputMarkupId(true);
            }
        };
        cellsContainer.add(cellRepeater);

        add(cellsContainer);
    }

}
