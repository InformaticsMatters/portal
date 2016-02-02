package portal.notebook.cells;

import com.im.lac.job.jobdef.*;
import org.squonk.notebook.api.VariableKey;
import org.squonk.client.JobStatusClient;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import portal.notebook.api.*;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by timbo on 16/01/16.
 */
public abstract class AbstractJobCellExecutor implements CellExecutor, Serializable {


    public JobStatus execute(CellExecutionData data) throws Exception {
        String username = "curentuser"; // get the user
        Integer workunits = null; // null means "I don't know", but we can probably get the number from the dataset metadata

        // create the job
        JobDefinition jobdef = buildJobDefinition(data);
        // execute the job
        JobStatusClient client = createJobStatusClient();
        JobStatus status = client.submit(jobdef, username, workunits);
        // job is now running. we can either poll the JobStatusRestClient for its status or listen on the message queue for updates
        return status;
    }

    protected JobStatusClient createJobStatusClient() {
        Instance<JobStatusClient> instance = CDI.current().select(JobStatusClient.class);
        return instance.get();
    }

    /**
     * Build the JobDefinition that will be submitted for execution.
     */
    protected abstract JobDefinition buildJobDefinition(CellExecutionData cellExecutionData);


    protected Map<String, Object> collectAllOptions(CellInstance cell) {
        return cell.getOptionMap().entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue() == null ? null : e.getValue().getValue()));
    }

    protected VariableKey createVariableKey(CellInstance cell, String varName) {
        BindingInstance binding = cell.getBindingMap().get(varName);
        if (binding != null) {
            VariableInstance variable = binding.getVariable();
            if (variable != null) {
                return new VariableKey(variable.getName(), variable.getName());
            }
        }
        return null;
    }

    protected VariableKey createVariableKeyRequired(CellInstance cell, String varName) {
        VariableKey key = createVariableKey(cell, varName);
        if (key == null) {
            throw new IllegalStateException("Input variable " + varName + " not bound");
        }
        return key;
    }

    /**
     * Build the JobDefintion using the specified StepDefinition(s).
     *
     * @param notebookId
     * @param cell
     * @param steps
     * @return
     */
    protected StepsCellExecutorJobDefinition buildJobDefinition(Long notebookId, CellInstance cell, StepDefinition... steps) {
        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition();
        jobdef.configureCellAndSteps(notebookId, cell.getName(), steps);
        return jobdef;
    }

    public static class MockExecutor1 implements CellExecutor {

        @Override
        public JobStatus execute(CellExecutionData cellExecutionData) throws Exception {
            // do something to set the output variable(s)
            // create a fake jobstatus
            return JobStatus.create(null /* JobDefinition */, "username", new Date(), null).withStatus(JobStatus.Status.COMPLETED, 0, 0, null);
        }
    }

    public static class ChemblActivitiesFetcher extends AbstractJobCellExecutor {

        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_CHEMBL_ACTIVITIES_FETCHER)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cellInstance));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }

    public static class DatasetMerger extends AbstractJobCellExecutor {

        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_DATASET_MERGER)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cellInstance));

            for (int i = 1; i <= 5; i++) {
                VariableKey key = createVariableKey(cellInstance, "input" + i);
                if (key != null) {
                    step1.withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET + i, key);
                } else {
                    break;
                }
            }
            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }

    public static class BasicObjectToMoleculeObjectConvertor extends AbstractJobCellExecutor {

        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_BASICOBJECT_TO_MOLECULEOBJECT)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, createVariableKeyRequired(cellInstance, DefaultCellDefinitionRegistry.VAR_NAME_INPUT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cellInstance));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }

    public static class CSVFileUploader extends AbstractJobCellExecutor {

        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_CSV_READER)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, new VariableKey(cellInstance.getName(), DefaultCellDefinitionRegistry.VAR_NAME_FILECONTENT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cellInstance));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }

    public static class SDFileUploader extends AbstractJobCellExecutor {

        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_SDF_READER)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, new VariableKey(cellInstance.getName(), DefaultCellDefinitionRegistry.VAR_NAME_FILECONTENT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cellInstance));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }

    public static class ValueTransformer extends AbstractJobCellExecutor {

        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_VALUE_TRANSFORMER)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, createVariableKeyRequired(cellInstance, DefaultCellDefinitionRegistry.VAR_NAME_INPUT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cellInstance));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }

    public static class GroovyScriptTrusted extends AbstractJobCellExecutor {

        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_TRUSTED_GROOVY_DATASET_SCRIPT)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, createVariableKeyRequired(cellInstance, DefaultCellDefinitionRegistry.VAR_NAME_INPUT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cellInstance));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }
}