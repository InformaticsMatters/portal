package portal.notebook;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.execution.api.CellType;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookCellTypesPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "cellDescriptor";

    private WebMarkupContainer descriptorssContainer;
    private ListView<CellType> descriptorRepeater;

    @Inject
    private NotebookSession notebookSession;

    public NotebookCellTypesPanel(String id) {
        super(id);
        addCells();
    }

    private void addCells() {
        descriptorssContainer = new WebMarkupContainer("descriptorsContainer");
        descriptorssContainer.setOutputMarkupId(true);

        List<CellType> cells = notebookSession.listCellDescriptor();
        descriptorRepeater = new ListView<CellType>("descriptor", cells) {

            @Override
            protected void populateItem(ListItem<CellType> listItem) {
                CellType cellType = listItem.getModelObject();
                listItem.add(new NotebookCellTypePanel("descriptorItem", cellType));
                listItem.setOutputMarkupId(true);
                listItem.add(new AttributeModifier(NotebookCanvasPage.DROP_DATA_TYPE, DROP_DATA_TYPE_VALUE));
                listItem.add(new AttributeModifier(NotebookCanvasPage.DROP_DATA_ID, cellType.getName()));
            }
        };
        descriptorssContainer.add(descriptorRepeater);

        add(descriptorssContainer);
    }

}
