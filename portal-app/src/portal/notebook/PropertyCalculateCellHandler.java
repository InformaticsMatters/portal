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
    public void execute(Cell cell) {
        try {
            List<MoleculeObject> list = notebookService.retrieveFileContentAsMolecules((String) cell.getInputVariableList().get(0).getValue());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(byteArrayOutputStream, list);
            byteArrayOutputStream.flush();
            FileOutputStream outputStream = new FileOutputStream("files/" + cell.getOutputVariableList().get(0).getValue());
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                try {
                    calculatorsClient.calculate(cell.getPropertyMap().get("serviceName").toString(), inputStream, outputStream);
                } catch (Throwable t) {
                    outputStream.write("[]".getBytes());
                }
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.PROPERTY_CALCULATE);
    }

}