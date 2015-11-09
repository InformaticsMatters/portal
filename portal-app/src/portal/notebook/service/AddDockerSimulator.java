package portal.notebook.service;

import portal.notebook.api.CellDTO;
import portal.notebook.api.CellExecutionClient;
import portal.notebook.api.NotebookDTO;

import javax.inject.Inject;


public class AddDockerSimulator {
    @Inject
    private CellExecutionClient cellExecutionClient;


    public void execute(String uriBase, Long notebookId, String cellName) throws Exception {
        cellExecutionClient.setUriBase(uriBase);
        NotebookDTO notebookDTO = cellExecutionClient.retrieveNotebookDefinition(notebookId);
        CellDTO cell = notebookDTO.findCell(notebookDTO, cellName);
        Integer num1 = (Integer) cell.getPropertyMap().get("num1");
        Integer num2 = (Integer) cell.getPropertyMap().get("num2");
        Integer result = (num1 == null || num2 == null) ? null : num1 + num2;
        String outputVariableName = cell.getOutputVariableNameList().get(0);
        cellExecutionClient.writeIntegerValue(notebookId, cellName, outputVariableName, result);
    }

}