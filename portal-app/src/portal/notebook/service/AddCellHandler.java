package portal.notebook.service;

import portal.notebook.api.CellType;
import portal.notebook.api.VariableType;
import toolkit.services.Transactional;

import javax.inject.Inject;

@Transactional
public class AddCellHandler implements CellHandler {
    @Inject
    private NotebookService notebookService;
    @Inject
    private CalculatorsClient calculatorsClient;


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
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Cell cell = notebookContents.findCell(cellName);
        Integer num1 = (Integer) cell.getPropertyMap().get("num1");
        Integer num2 = (Integer) cell.getPropertyMap().get("num2");
        Integer result = (num1 == null || num2 == null) ? null : num1 + num2;
        cell.getOutputVariableList().get(0).setValue(result);
        notebookService.storeNotebookContents(notebookId, notebookContents);
    }


    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.ADD);
    }

}