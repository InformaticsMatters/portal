package portal.notebook.service;

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.io.JsonHandler;
import portal.notebook.api.NotebookClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

@ApplicationScoped
@Path("jobs")
public class MockJobStatusService {
    private static final Logger LOGGER = Logger.getLogger(MockJobStatusService.class.getName());
    @Inject
    private NotebookClient notebookClient;

    @Path("execute")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void execute(JobDefinition jobDefinition) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
            notebookClient.writeStreamContents(notebookId, cellName, name, dataInputStream);
        }
        DatasetMetadata metadata = generator.getDatasetMetadata();
        String jsonTring = JsonHandler.getInstance().objectToJson(metadata);
        notebookClient.writeTextValue(notebookId, cellName, name, jsonTring);
        String storedValue = notebookClient.readTextValue(notebookId, cellName, name);
        if (!storedValue.equals(jsonTring)) {
            throw new RuntimeException("Storage failed. Returned values: " + storedValue);
        }
    }

    private JobStatus processChemblActivitiesFetcher(ExecuteCellUsingStepsJobDefinition jobDefinition) {
        Dataset<MoleculeObject> dataset = createMockDataset("mock");
        try {
            writeDataset(jobDefinition.getNotebookId(), jobDefinition.getCellName(), "output", dataset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    Dataset<MoleculeObject> createMockDataset(String prefix) {
        List<MoleculeObject> mols = new ArrayList<>();
        Map<String,Object> values = new HashMap<>();
        values.put("ID", 1);
        values.put(prefix, 1.1);
        mols.add(new MoleculeObject("C", "smiles", values));
        values.put("ID", 2);
        values.put(prefix, 2.2);
        mols.add(new MoleculeObject("CC", "smiles", values));
        values.put("ID", 3);
        values.put(prefix, 3.3);
        mols.add(new MoleculeObject("CCC", "smiles", values));
        return new Dataset<>(MoleculeObject.class, mols);
    }

}
