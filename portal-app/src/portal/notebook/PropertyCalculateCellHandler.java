package portal.notebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;

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
    public void execute(Notebook notebook, Cell cell) {
        try {
            propertyCaculateDockerSimulator.execute("http://localhost:8080/ws/cell", notebook.getId(), cell.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.PROPERTY_CALCULATE);
    }

}