package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class QndNotebookPanel extends Panel {

    private final Notebook notebook;
    private NotebookChangeListener notebookChnageListener;

    public QndNotebookPanel(String id, Notebook notebook) {
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
                RequestCycle.get().find(AjaxRequestTarget.class).add(QndNotebookPanel.this);
            }

            @Override
            public void onCellAdded(Cell cell) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(QndNotebookPanel.this);
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
                CanvasItemPanel<Cell> panel = createCellPanel("cell", descriptor);
                listItem.add(panel);
            }
        };
        add(listView);
    }

    private <T extends Cell> CanvasItemPanel<T> createCellPanel(String id, T cell) {
        try {
            try {
                Class cellClass = resolveCellClass(cell);
                return (CanvasItemPanel<T>) cellClass.getConstructor(String.class, Notebook.class, cell.getClass()).newInstance(id, notebook, cell);
            } catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Cell> Class<? extends CanvasItemPanel> resolveCellClass(T cell) {
        if (CellType.CODE.equals(cell.getCellType())) {
            return ScriptCanvasItemPanel.class;
        } else if (CellType.NOTEBOOK_DEBUG.equals(cell.getCellType())) {
            return NotebookDebugCanvasItemPanel.class;
        } else if (CellType.FILE_UPLOAD.equals(cell.getCellType())) {
            return FileUploadCanvasItemPanel.class;
        } else if (CellType.PROPERTY_CALCULATE.equals(cell.getCellType())) {
            return PropertyCalculateCanvasItemPanel.class;
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
            return ScriptCell.class;
        } else if (CellType.PROPERTY_CALCULATE.equals(cellType)) {
            return PropertyCalculateCell.class;
        } else {
            return null;
        }
    }

}
