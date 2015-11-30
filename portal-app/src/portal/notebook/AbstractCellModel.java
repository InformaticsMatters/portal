package portal.notebook;

import com.squonk.notebook.api.CellType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

public abstract class AbstractCellModel implements CellModel {
    private final CellType cellType;
    private String name;
    private int positionLeft;
    private int positionTop;

    public AbstractCellModel(CellType cellType) {
        this.cellType = cellType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(int x) {
        this.positionLeft = x;
    }

    @Override
    public int getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(int y) {
        this.positionTop = y;
    }


    @Override
    public void store(NotebookContents notebookContents, Cell cell) {
        storeHeader(cell);
        storeInputVariables(notebookContents, cell);
    }

    protected void storeHeader(Cell cell) {
        cell.setCellType(getCellType());
        cell.setPositionLeft(getPositionLeft());
        cell.setPositionTop(getPositionTop());
        cell.setName(getName());
    }

    protected void storeInputVariables(NotebookContents notebookContents, Cell cell) {
        for (VariableModel variableModel : getInputVariableModelList()) {
            Variable variable = notebookContents.findVariable(variableModel.getProducer().getName(), variableModel.getName());
            cell.getInputVariableList().add(variable);
        }
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        loadInputVariables(notebookModel, cell);
        loadOutputVariables(cell);
    }

    protected void loadHeader(Cell cell) {
        setName(cell.getName());
        positionLeft = cell.getPositionLeft();
        positionTop = cell.getPositionTop();
    }

    protected void loadOutputVariables(Cell cell) {
        getOutputVariableNameList().clear();
        for (Variable variable : cell.getOutputVariableList())  {
            getOutputVariableNameList().add(variable.getName());
        }
    }

    protected void loadInputVariables(NotebookModel notebookModel, Cell cell) {
        getInputVariableModelList().clear();
        for (Variable variable : cell.getInputVariableList())  {
             VariableModel variableModel = notebookModel.findVariable(variable.getProducerCell().getName(), variable.getName());
             getInputVariableModelList().add(variableModel);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        return ((CellModel)o).getName().equals(getName());
    }

    @Override
    public CellType getCellType() {
        return cellType;
    }
}
