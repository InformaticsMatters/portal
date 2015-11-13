package portal.notebook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import portal.notebook.api.*;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyCalculateCellHandler implements CellHandler {
    @Inject
    private NotebookService notebookService;
    @Inject
    private CalculatorsClient calculatorsClient;
    @Inject
    private CellExecutionClient cellExecutionClient;

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
    public void execute(String cellName) {
        try {
            NotebookDTO notebookDefinition = cellExecutionClient.retrieveNotebookDefinition();
            CellDTO cellDefinition = findCell(notebookDefinition, cellName);
            VariableDTO inputVariableDefinition = cellDefinition.getInputVariableList().get(0);

            //special case for VariableType FILE: text value is the file name, file contents accessed through stream API
            String fileName = cellExecutionClient.readTextValue(inputVariableDefinition.getProducerName(), inputVariableDefinition.getName());
            InputStream inputStream = cellExecutionClient.readStreamValue(inputVariableDefinition.getProducerName(), inputVariableDefinition.getName());

            List<MoleculeObject> molecules = parseFileStream(fileName, inputStream);
            ByteArrayOutputStream moleculesOutputStream = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(moleculesOutputStream, molecules);
            moleculesOutputStream.flush();

            String serviceName = (String) cellDefinition.getPropertyMap().get("serviceName");
            byte[] resultBytes = calculate(moleculesOutputStream.toByteArray(), serviceName);

            String outputVariableName = cellDefinition.getOutputVariableNameList().get(0);
            cellExecutionClient.writeStreamContents(cellName, outputVariableName, new ByteArrayInputStream(resultBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private byte[] calculate(byte[] bytes, String serviceName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        try {
            calculatorsClient.calculate(serviceName, inputStream, outputStream);
        } catch (Throwable t) {
            outputStream.write("[]".getBytes());
        }
        outputStream.flush();
        return outputStream.toByteArray();
    }

    private CellDTO findCell(NotebookDTO notebookDefinition, String cellName) {
        for (CellDTO cellDefinition : notebookDefinition.getCellList()) {
            if (cellDefinition.getName().equals(cellName)) {
                return cellDefinition;
            }
        }
        return null;
    }

    public List<MoleculeObject> parseFileStream(String fileName, InputStream inputStream) throws Exception {
        int x = fileName.lastIndexOf(".");
        String ext = fileName.toLowerCase().substring(x + 1);
        if (ext.equals("json")) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream, new TypeReference<List<MoleculeObject>>() {
            });
        } else if (ext.equals("tab")) {
            return parseTsv(inputStream);
        } else {
            return new ArrayList<>();
        }
    }

    private List<MoleculeObject> parseTsv(InputStream inputStream) throws IOException {
        List<MoleculeObject> list = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = bufferedReader.readLine();
        String[] headers = line.split("\t");
        for (int h = 0; h < headers.length; h++) {
            headers[h] = trim(headers[h]);
        }
        while (line != null) {
            line = line.trim();
            String[] columns = line.split("\t");
            String value = columns[0].trim();
            String smile = value.substring(1, value.length() - 1);
            MoleculeObject object = new MoleculeObject(smile);
            for (int i = 1; i < columns.length; i++) {
                String name = headers[i];
                String prop = trim(columns[i]);
                object.putValue(name, prop);
            }
            list.add(object);
            line = bufferedReader.readLine();
        }
        return list;
    }


    private String trim(String v) {
        if (v.length() > 1 && v.charAt(0) == '"' && v.charAt(v.length() - 1) == '"') {
            return v.substring(1, v.length() - 1);
        } else {
            return v;
        }
    }


    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.PROPERTY_CALCULATE);
    }

}