package portal.webapp.notebook;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class NotebookPanel extends Panel {

    private final Notebook notebook;

    public NotebookPanel(String id, Notebook notebook) {
        super(id);
        this.notebook = notebook;
        setOutputMarkupId(true);
        addCells();
    }

    private void addCells() {
        List<Cell> list = notebook.getCellList();
        ListView<Cell> listView = new ListView<Cell>("item", list) {
            @Override
            protected void populateItem(ListItem<Cell> listItem) {
                Cell descriptor = listItem.getModelObject();
                CellPanel<Cell> panel = createCellPanel("cell", descriptor);
                listItem.add(panel);
            }
        };
        add(listView);
    }

    private <T extends Cell> CellPanel<T> createCellPanel(String id, T cellDescriptor) {
        try {
            try {
                Class cellClass = resolveCellClass(cellDescriptor);
                return (CellPanel<T>) cellClass.getConstructor(String.class, Notebook.class, cellDescriptor.getClass()).newInstance(id, notebook, cellDescriptor);
            } catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Cell> Class<? extends CellPanel> resolveCellClass(T cellDescriptor) {
        if (CellType.CODE.equals(cellDescriptor.getCellType())) {
            return CodeCellPanel.class;
        } else if (CellType.DEBUG.equals(cellDescriptor.getCellType())) {
            return NotebookDebugCellPanel.class;
        } else {
            return null;
        }
    }

    public void addNewCell(CellType cellType, int x, int y) {
        Class<?extends Cell> descriptorClass = resolveDescriptorClass(cellType);
        try {
            Cell descriptor = descriptorClass.newInstance();
            descriptor.setX(x);
            descriptor.setY(y);
            notebook.getCellList().add(descriptor);
            //refresh list!!!!!!!!!!!
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<? extends Cell> resolveDescriptorClass(CellType cellType) {
        if (CellType.DEBUG.equals(cellType)) {
            return NotebookDebugCell.class;
        } else if (CellType.CODE.equals(cellType)) {
            return CodeCell.class;
        } else {
            return null;
        }
    }

}
