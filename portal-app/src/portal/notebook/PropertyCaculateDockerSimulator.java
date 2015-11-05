package portal.notebook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyCaculateDockerSimulator {
    @Inject
    private CellExecutionClient cellExecutionClient;
    @Inject
    private CalculatorsClient calculatorsClient;


    public void execute(String uriBase, Long notebookId, String cellName) throws Exception {
        cellExecutionClient.setUriBase(uriBase);
        NotebookDefinitionDTO notebookDefinition = cellExecutionClient.retrieveNotebookDefinition(notebookId);
        CellDefinitionDTO cellDefinition = findCell(notebookDefinition, cellName);
        VariableDefinitionDTO inputVariableDefinition = cellDefinition.getInputVariableDefinitionList().get(0);

        String fileName = cellExecutionClient.readTextValue(notebookId, inputVariableDefinition.getProducerName(), inputVariableDefinition.getName());
        InputStream inputStream = cellExecutionClient.readStreamValue(notebookId, inputVariableDefinition.getProducerName(), inputVariableDefinition.getName());

        List<MoleculeObject> molecules = parseFileStream(fileName, inputStream);
        ByteArrayOutputStream moleculesOutputStream = new ByteArrayOutputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(moleculesOutputStream, molecules);
        moleculesOutputStream.flush();
        byte[] resultBytes = calculate(moleculesOutputStream.toByteArray(), cellDefinition);

        String outputVariableName = cellDefinition.getOutputVariableNameList().get(0);
        cellExecutionClient.writeStreamValue(notebookId, cellName, outputVariableName, new ByteArrayInputStream(resultBytes));

    }

    private byte[] calculate(byte[] bytes, CellDefinitionDTO cellDefinition) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        try {
            calculatorsClient.calculate(cellDefinition.getPropertyMap().get("serviceName").toString(), inputStream, outputStream);
        } catch (Throwable t) {
            outputStream.write("[]".getBytes());
        }
        outputStream.flush();
        return outputStream.toByteArray();
    }

    private CellDefinitionDTO findCell(NotebookDefinitionDTO notebookDefinition, String cellName) {
        for (CellDefinitionDTO cellDefinition : notebookDefinition.getCellDefinitionList()) {
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
            return objectMapper.readValue(inputStream, new TypeReference<List<MoleculeObject>>() {});
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


}
