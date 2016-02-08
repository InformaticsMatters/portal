package portal.notebook.cells;

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobQuery;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.types.MoleculeObject;
import org.squonk.client.JobStatusClient;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.notebook.client.CallbackContext;
import org.squonk.types.io.JsonHandler;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Alternative
public class MockJobStatusClient implements JobStatusClient, Serializable {
    @Inject
    private CallbackClient callbackClient;
    @Inject
    private CallbackContext callbackContext;


    @Override
    public JobStatus submit(JobDefinition jobDefinition, String s, Integer integer) throws IOException {
        if (jobDefinition instanceof ExecuteCellUsingStepsJobDefinition) {
            ExecuteCellUsingStepsJobDefinition executeCellUsingStepsJobDefinition = (ExecuteCellUsingStepsJobDefinition)jobDefinition;
            callbackContext.setNotebookId(executeCellUsingStepsJobDefinition.getNotebookId());
            return processStepsJobDefinition(executeCellUsingStepsJobDefinition);
        } else {
            return null;
        }
    }

    private JobStatus processStepsJobDefinition(ExecuteCellUsingStepsJobDefinition jobDefinition) {
        if (jobDefinition.getSteps()[0].getImplementationClass().equals(StepDefinitionConstants.ChemblActivitiesFetcher.CLASSNAME)) {
            return processChemblActivitiesFetcher(jobDefinition);
        } else {
            return null;
        }
    }

    protected void writeDataset(String cellName, String name, Dataset dataset) throws IOException {
        Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
        try (Stream stream = generator.getAsStream()) {
            InputStream dataInputStream = generator.getAsInputStream(stream, true);
            callbackClient.writeStreamContents(cellName, name, dataInputStream);
        }
        DatasetMetadata metadata = generator.getDatasetMetadata();
        String jsonTring = JsonHandler.getInstance().objectToJson(metadata);
        callbackClient.writeTextValue(cellName, name, jsonTring);
        String storedValue = callbackClient.readTextValue(cellName, name);
        if (!storedValue.equals(jsonTring)) {
            throw new RuntimeException("Storage failed. Returned values: " + storedValue);
        }
    }

    private JobStatus processChemblActivitiesFetcher(ExecuteCellUsingStepsJobDefinition jobDefinition) {
        Dataset<MoleculeObject> dataset = createMockDataset("mock");
        try {
            writeDataset(jobDefinition.getCellName(), "output", dataset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    Dataset<MoleculeObject> createMockDataset(String prefix) {
        List<MoleculeObject> mols = new ArrayList<>();
        Map values = new HashMap();
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



    @Override
    public JobStatus get(String s) throws IOException {
        return null;
    }

    @Override
    public List<JobStatus> list(JobQuery jobQuery) throws IOException {
        return null;
    }

    @Override
    public JobStatus updateStatus(String s, JobStatus.Status status, String s1, Integer integer, Integer integer1) throws IOException {
        return null;
    }

    @Override
    public JobStatus incrementCounts(String s, int i, int i1) throws IOException {
        return null;
    }
}
