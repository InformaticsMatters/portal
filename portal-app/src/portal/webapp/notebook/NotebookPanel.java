package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class NotebookPanel extends Panel {

    private final Notebook notebook;
    private NotebookChangeListener notebookChnageListener;

    public NotebookPanel(String id, Notebook notebook) {
        super(id);
        this.notebook = notebook;
        setOutputMarkupId(true);
        addCells();
        addListeners();
    }

    private void addListeners() {
        notebookChnageListener = new NotebookChangeListener() {
            @Override
            public void onCellRemoved(Cell cell) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(NotebookPanel.this);
            }

            @Override
            public void onCellAdded(Cell cell) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(NotebookPanel.this);
            }
        };
        notebook.addNotebookChangeListener(notebookChnageListener);
    }

    private void addCells() {
        IModel<List<Cell>> listModel = new IModel<List<Cell>>() {
            @Override
            public void detach() {

            }

            @Override
            public List<Cell> getObject() {
                return notebook.getCellList();
            }

            @Override
            public void setObject(List<Cell> cells) {

            }
        };
        ListView<Cell> listView = new ListView<Cell>("item", listModel) {
            @Override
            protected void populateItem(ListItem<Cell> listItem) {
                Cell descriptor = listItem.getModelObject();
                CellPanel<Cell> panel = createCellPanel("cell", descriptor);
                listItem.add(panel);
            }
        };
        add(listView);
    }

    private <T extends Cell> CellPanel<T> createCellPanel(String id, T cell) {
        try {
            try {
                Class cellClass = resolveCellClass(cell);
                return (CellPanel<T>) cellClass.getConstructor(String.class, Notebook.class, cell.getClass()).newInstance(id, notebook, cell);
            } catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Cell> Class<? extends CellPanel> resolveCellClass(T cell) {
        if (CellType.CODE.equals(cell.getCellType())) {
            return CodeCellPanel.class;
        } else if (CellType.NOTEBOOK_DEBUG.equals(cell.getCellType())) {
            return NotebookDebugCellPanel.class;
        } else if (CellType.QND_PRODUCER.equals(cell.getCellType())) {
            return QndProducerCellPanel.class;
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
        if (CellType.NOTEBOOK_DEBUG.equals(cellType)) {
            return NotebookDebugCell.class;
        } else if (CellType.CODE.equals(cellType)) {
            return CodeCell.class;
        } else {
            return null;
        }
    }

}
