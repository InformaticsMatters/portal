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
                Class cellClass = resolveCellClass(cellDescriptor);
                return (CellPanel<T>) cellClass.getConstructor(String.class, NotebookDescriptor.class, cellDescriptor.getClass()).newInstance(id, notebookDescriptor, cellDescriptor);
            } catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends CellDescriptor> Class<? extends CellPanel> resolveCellClass(T cellDescriptor) {
        if (CellType.CODE.equals(cellDescriptor.getCellType())) {
            return CodeCellPanel.class;
        } else if (CellType.DEBUG.equals(cellDescriptor.getCellType())) {
            return NotebookDebugCellPanel.class;
        } else {
            return null;
        }
    }

    public void addNewCell(CellType cellType, int x, int y) {
        Class<?extends CellDescriptor> descriptorClass = resolveDescriptorClass(cellType);
        try {
            CellDescriptor descriptor = descriptorClass.newInstance();
            descriptor.setX(x);
            descriptor.setY(y);
            notebookDescriptor.getCellDescriptorList().add(descriptor);
            //refresh list!!!!!!!!!!!
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<? extends CellDescriptor> resolveDescriptorClass(CellType cellType) {
        if (CellType.DEBUG.equals(cellType)) {
            return NotebookDebugCellDescriptor.class;
        } else if (CellType.CODE.equals(cellType)) {
            return CodeCellDescriptor.class;
        } else {
            return null;
        }
    }

}
