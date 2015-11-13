package portal.notebook.service;

import portal.notebook.api.CellExecutionContext;
import portal.notebook.api.CellType;
import portal.notebook.api.VariableType;
import toolkit.services.Transactional;

import javax.inject.Inject;

@Transactional
public class Sample1CellHandler implements CellHandler {
    @Inject
    private NotebookService notebookService;
    @Inject
    private CellExecutionContext cellExecutionContext;

    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.SAMPLE1);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("result");
        variable.setVariableType(VariableType.VALUE);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(String cellName) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(cellExecutionContext.getNotebookId());
        Cell cell = notebookContents.findCell(cellName);
        Variable inputVariable = cell.getInputVariableList().get(0);
        Integer num1 = (Integer) inputVariable.getValue();
        Integer num2 = (Integer) cell.getPropertyMap().get("num2");
        Integer result = (num1 == null || num2 == null) ? null : num1 + num2;
        Variable outputVariable = cell.getOutputVariableList().get(0);
        outputVariable.setValue(result);
        notebookService.storeNotebookContents(cellExecutionContext.getNotebookId(), notebookContents);
    }


    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.SAMPLE1);
    }

}