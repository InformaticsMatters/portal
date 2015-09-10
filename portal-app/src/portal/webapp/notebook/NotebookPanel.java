package portal.webapp.notebook;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class NotebookPanel extends Panel {

    private final NotebookDescriptor notebookDescriptor;

    public NotebookPanel(String id, NotebookDescriptor notebookDescriptor) {
        super(id);
        this.notebookDescriptor = notebookDescriptor;
        setOutputMarkupId(true);
        addCells();
    }

    private void addCells() {
        List<CellDescriptor> list = notebookDescriptor.getCellDescriptorList();
        ListView<CellDescriptor> listView = new ListView<CellDescriptor>("item", list) {
            @Override
            protected void populateItem(ListItem<CellDescriptor> listItem) {
                CellDescriptor descriptor = listItem.getModelObject();
                CellPanel<CellDescriptor> panel = createCellPanel("cell", descriptor);
                listItem.add(panel);
            }
        };
        add(listView);
    }

    private <T extends CellDescriptor> CellPanel<T> createCellPanel(String id, T cellDescriptor) {
        try {
            try {
                return (CellPanel<T>) cellDescriptor.getCellClass().getConstructor(String.class, NotebookDescriptor.class, cellDescriptor.getClass()).newInstance(id, notebookDescriptor, cellDescriptor);
            } catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
