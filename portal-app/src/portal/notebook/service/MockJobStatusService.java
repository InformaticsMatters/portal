package portal.notebook.service;

import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition;
import org.squonk.jobdef.JobDefinition;
import org.squonk.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.notebook.api.NotebookEditableDTO;
import org.squonk.reader.SDFReader;
import org.squonk.types.PDBFile;
import org.squonk.types.io.JsonHandler;
import portal.notebook.api.CellDefinition;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
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
        } else if (jobDefinition.getSteps()[0].getImplementationClass().equals(StepDefinitionConstants.PdbUpload.CLASSNAME)) {
            //processPdbUpload(jobDefinition);
        }
    }


    private void processChemblActivitiesFetcher(ExecuteCellUsingStepsJobDefinition jobDefinition) throws Exception {
        List<NotebookEditableDTO> editableList = notebookVariableClient.listEditables(jobDefinition.getNotebookId(), "user1");
        NotebookEditableDTO editableDTO = editableList.get(0);
        NotebookCanvasDTO notebookCanvasDTO = editableDTO.getCanvasDTO();
        NotebookCanvasDTO.CellDTO cellDTO = findCell(notebookCanvasDTO, jobDefinition.getCellId());
        String prefix = (String)cellDTO.getOptions().get("prefix");
        if (prefix == null) {
            throw new Exception("Null prefix");
        }
        Dataset<MoleculeObject> dataset = createMockDataset(prefix);
        writeDataset(jobDefinition.getNotebookId(), jobDefinition.getEditableId(), jobDefinition.getCellId(), "output", dataset);
    }

    private void processSdfUpload(ExecuteCellUsingStepsJobDefinition jobDefinition) throws Exception {
        try (InputStream inputStream = notebookVariableClient.readStreamValue(jobDefinition.getNotebookId(), jobDefinition.getEditableId(), jobDefinition.getCellId(), CellDefinition.VAR_NAME_FILECONTENT)) {
            SDFReader reader = new SDFReader(inputStream);
            MoleculeObjectDataset modataset = new MoleculeObjectDataset(reader.asStream());
            writeDataset(jobDefinition.getNotebookId(), jobDefinition.getEditableId(), jobDefinition.getCellId(), CellDefinition.VAR_NAME_OUTPUT, modataset.getDataset());
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
        Random random = new Random();
        for (int i = 0; i < 500; i++) {
            Map<String, Object> values = new LinkedHashMap<>();

            values.put("ID", i + 1);
            values.put(prefix, 1.1);
            values.put("x Axis", random.nextInt(19) - 10);
            values.put("y Axis", random.nextInt(19) - 10);
            values.put("Color", random.nextInt(5) + 1);
            values.put("row", "row" + random.nextInt(10));
            values.put("col", "col" + random.nextInt(10));

            mols.add(new MoleculeObject("Cn1cnc2n(C)c(=O)n(C)c(=O)c12", "smiles", values));
        }
        return new Dataset<>(MoleculeObject.class, mols);
    }
}
