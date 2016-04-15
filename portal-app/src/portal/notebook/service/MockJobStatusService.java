package portal.notebook.service;

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.io.JsonHandler;
import portal.notebook.api.CellInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

@ApplicationScoped
@Path("mockJobs")
public class MockJobStatusService {

    private static final Logger LOGGER = Logger.getLogger(MockJobStatusService.class.getName());
    @Inject
    private MockNotebookClient mockNotebookClient;

    @Path("execute")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void execute(JobDefinition jobDefinition) {
        if (jobDefinition instanceof ExecuteCellUsingStepsJobDefinition) {
            ExecuteCellUsingStepsJobDefinition executeCellUsingStepsJobDefinition = (ExecuteCellUsingStepsJobDefinition) jobDefinition;
            processStepsJobDefinition(executeCellUsingStepsJobDefinition);
        }
        LOGGER.info("Job definition processed");
    }

    private JobStatus processStepsJobDefinition(ExecuteCellUsingStepsJobDefinition jobDefinition) {
        if (jobDefinition.getSteps()[0].getImplementationClass().equals(StepDefinitionConstants.ChemblActivitiesFetcher.CLASSNAME)) {
            return processChemblActivitiesFetcher(jobDefinition);
        } else {
            return null;
        }
    }

    protected void writeDataset(Long notebookId, String cellName, String name, Dataset dataset) throws IOException {
        Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
        try (Stream stream = generator.getAsStream()) {
            InputStream dataInputStream = generator.getAsInputStream(stream, true);
            mockNotebookClient.oldWriteStreamValue(notebookId, cellName, name, dataInputStream);
        }
        DatasetMetadata metadata = generator.getDatasetMetadata();
        String jsonTring = JsonHandler.getInstance().objectToJson(metadata);
        mockNotebookClient.oldWriteTextValue(notebookId, cellName, name, jsonTring);
        String storedValue = mockNotebookClient.oldReadTextValue(notebookId, cellName, name);
        if (!storedValue.equals(jsonTring)) {
            throw new RuntimeException("Storage failed. Returned values: " + storedValue);
        }
    }

    private JobStatus processChemblActivitiesFetcher(ExecuteCellUsingStepsJobDefinition jobDefinition) {
        CellInstance cellInstance = mockNotebookClient.oldFindCellInstance(jobDefinition.getNotebookId(), jobDefinition.getCellName());
        Dataset<MoleculeObject> dataset = createMockDataset((String)cellInstance.getOptionInstanceMap().get("prefix").getValue());
        try {
            writeDataset(jobDefinition.getNotebookId(), jobDefinition.getCellName(), "output", dataset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    Dataset<MoleculeObject> createMockDataset(String prefix) {
        List<MoleculeObject> mols = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("ID", i + 1);
            values.put(prefix, 1.1);
            values.put("x Axis", new Random().nextInt(19) - 5);
            values.put("y Axis", new Random().nextInt(19) + 1);
            values.put("Color", new Random().nextInt(5) + 1);
            mols.add(new MoleculeObject("Cn1cnc2n(C)c(=O)n(C)c(=O)c12", "smiles", values));
        }
        return new Dataset<>(MoleculeObject.class, mols);
    }
}
