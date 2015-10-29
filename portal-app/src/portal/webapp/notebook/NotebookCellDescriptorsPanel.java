package portal.webapp.notebook;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.CellDescriptor;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookCellDescriptorsPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "cellDescriptor";

    private WebMarkupContainer descriptorssContainer;
    private ListView<CellDescriptor> descriptorRepeater;

    @Inject
    private NotebooksSession notebooksSession;

    public NotebookCellDescriptorsPanel(String id) {
        super(id);
        addCells();
    }

    private void addCells() {
        descriptorssContainer = new WebMarkupContainer("descriptorsContainer");
        descriptorssContainer.setOutputMarkupId(true);

        List<CellDescriptor> cells = notebooksSession.listCellDescriptor();
        descriptorRepeater = new ListView<CellDescriptor>("descriptor", cells) {

            @Override
            protected void populateItem(ListItem<CellDescriptor> listItem) {
                CellDescriptor cellDescriptor = listItem.getModelObject();
                listItem.add(new NotebookCellDescriptorPanel("descriptorItem", cellDescriptor));
                listItem.setOutputMarkupId(true);
                listItem.add(new AttributeModifier(NotebookCanvasPage.DROP_DATA_TYPE, DROP_DATA_TYPE_VALUE));
                listItem.add(new AttributeModifier(NotebookCanvasPage.DROP_DATA_ID, cellDescriptor.getCellType()));
            }
        };
        descriptorssContainer.add(descriptorRepeater);

        add(descriptorssContainer);
    }

}
