package portal.notebook.service;

import portal.notebook.api.CellType;
import portal.notebook.api.VariableType;

import javax.inject.Inject;

public class AddCellHandler implements CellHandler {
    @Inject
    private NotebookService notebookService;
    @Inject
    private AddDockerSimulator dockerSimulator;


    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.ADD);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("result");
        variable.setVariableType(VariableType.VALUE);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(Long notebookId, String cellName) {
        try {
            dockerSimulator.execute("http://localhost:8080/ws/cell", notebookId, cellName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.ADD);
    }

}