package portal.notebook.service;

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.types.MoleculeObject;
import org.squonk.client.NotebookVariableClient;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.notebook.api.NotebookEditableDTO;
import org.squonk.types.io.JsonHandler;
import portal.notebook.api.CellDefinition;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.wsdl.Input;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

@ApplicationScoped
@Path("mockJobs")
public class MockJobStatusService {

    private static final Logger LOGGER = Logger.getLogger(MockJobStatusService.class.getName());
    @Inject
    private MockNotebookVariableClient notebookVariableClient;

    @Path("execute")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void execute(JobDefinition jobDefinition) {
        try {
            if (jobDefinition instanceof ExecuteCellUsingStepsJobDefinition) {
                ExecuteCellUsingStepsJobDefinition executeCellUsingStepsJobDefinition = (ExecuteCellUsingStepsJobDefinition) jobDefinition;
                processStepsJobDefinition(executeCellUsingStepsJobDefinition);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void processStepsJobDefinition(ExecuteCellUsingStepsJobDefinition jobDefinition) throws Exception {
        if (jobDefinition.getSteps()[0].getImplementationClass().equals(StepDefinitionConstants.ChemblActivitiesFetcher.CLASSNAME)) {
            processChemblActivitiesFetcher(jobDefinition);
        } else if (jobDefinition.getSteps()[0].getImplementationClass().equals(StepDefinitionConstants.SdfUpload.CLASSNAME)) {
            processSdfUpload(jobDefinition);
        }
    }


    private void processChemblActivitiesFetcher(ExecuteCellUsingStepsJobDefinition jobDefinition) throws Exception {
        List<NotebookEditableDTO> editableList = notebookVariableClient.listEditables(jobDefinition.getNotebookId(), "user1");
        NotebookEditableDTO editableDTO = editableList.get(0);
        NotebookCanvasDTO notebookCanvasDTO = editableDTO.getCanvasDTO();
        NotebookCanvasDTO.CellDTO cellDTO = findCell(notebookCanvasDTO, jobDefinition.getCellId());
        Dataset<MoleculeObject> dataset = createMockDataset((String)cellDTO.getOptions().get("prefix"));
        try {
            writeDataset(jobDefinition.getNotebookId(), jobDefinition.getEditableId(), jobDefinition.getCellId(), "output", dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void processSdfUpload(ExecuteCellUsingStepsJobDefinition jobDefinition) throws Exception {
        InputStream inputStream = notebookVariableClient.readStreamValue(jobDefinition.getNotebookId(), jobDefinition.getEditableId(), jobDefinition.getCellId(), CellDefinition.VAR_NAME_FILECONTENT);
        try {
            //convert stream
            Dataset<MoleculeObject> dataset = createMockDataset("mock");
            writeDataset(jobDefinition.getNotebookId(), jobDefinition.getEditableId(), jobDefinition.getCellId(), CellDefinition.VAR_NAME_OUTPUT, dataset);
        } finally {
            inputStream.close();
        }
    }


    protected void writeDataset(Long notebookId, Long editableId, Long cellId, String name, Dataset dataset) throws Exception {
        Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
        try (Stream stream = generator.getAsStream()) {
            InputStream dataInputStream = generator.getAsInputStream(stream, true);
            notebookVariableClient.writeStreamValue(notebookId, editableId, cellId, name, dataInputStream);
        }
        DatasetMetadata metadata = generator.getDatasetMetadata();
        String jsonTring = JsonHandler.getInstance().objectToJson(metadata);
        notebookVariableClient.writeTextValue(notebookId, editableId, cellId, name, jsonTring);
        String storedValue = notebookVariableClient.readTextValue(notebookId, editableId, cellId, name);
        if (!jsonTring.equals(storedValue)) {
            throw new RuntimeException("Storage failed. Returned values: " + storedValue);
        }
    }

    private NotebookCanvasDTO.CellDTO findCell(NotebookCanvasDTO notebookCanvasDTO, Long cellId) {
        for (NotebookCanvasDTO.CellDTO cellDTO : notebookCanvasDTO.getCells()) {
            if (cellDTO.getId().equals(cellId)) {
                return cellDTO;
            }
        }
        return null;
    }

    Dataset<MoleculeObject> createMockDataset(String prefix) {
        List<MoleculeObject> mols = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("ID", i + 1);
            values.put(prefix, 1.1);
            values.put("x", new Random().nextInt(19) + 1);
            values.put("y", new Random().nextInt(19) + 1);
            mols.add(new MoleculeObject("Cn1cnc2n(C)c(=O)n(C)c(=O)c12", "smiles", values));
        }
        return new Dataset<>(MoleculeObject.class, mols);
    }
}
