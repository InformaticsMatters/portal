package portal.notebook.service;

import portal.notebook.api.CellType;
import portal.notebook.api.VariableType;

import javax.inject.Inject;

public class Sample2CellHandler implements CellHandler {
    @Inject
    private NotebookService notebookService;


    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.SAMPLE2);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("number");
        variable.setVariableType(VariableType.VALUE);
        variable.setValue(1);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(Long notebookId, String cellName) {

    }


    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.SAMPLE2);
    }

}