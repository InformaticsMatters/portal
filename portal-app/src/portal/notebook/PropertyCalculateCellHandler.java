package portal.notebook;

import javax.inject.Inject;

public class PropertyCalculateCellHandler implements CellHandler {
    @Inject
    private NotebookService notebookService;
    @Inject
    private CalculatorsClient calculatorsClient;
    @Inject
    private PropertyCaculateDockerSimulator propertyCaculateDockerSimulator;

    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.PROPERTY_CALCULATE);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("outputFile");
        variable.setVariableType(VariableType.FILE);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(Long notebookId, String cellName) {
        try {
            propertyCaculateDockerSimulator.execute("http://localhost:8080/ws/cell", notebookId, cellName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.PROPERTY_CALCULATE);
    }

}