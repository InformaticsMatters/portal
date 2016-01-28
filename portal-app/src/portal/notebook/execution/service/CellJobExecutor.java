package portal.notebook.execution.service;

import com.im.lac.job.client.JobStatusRestClient;
import com.im.lac.job.jobdef.*;
import org.squonk.client.JobStatusClient;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import portal.notebook.CellModel;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by timbo on 16/01/16.
 */
public abstract class CellJobExecutor implements CellExecutor {


    public JobStatus execute(Long notebookId, CellInstance cell) throws Exception {
        String username = "curentuser"; // get the user
        Integer workunits = null; // null means "I don't know", but we can probably get the number from the dataset metadata

        // create the job
        JobDefinition jobdef = buildJobDefinition(notebookId, cell);
        // execute the job
        JobStatusClient client = new JobStatusRestClient(); // presumably this is injected or obtained from somewhere
        JobStatus status = client.submit(jobdef, username, workunits);
        // job is now running. we can either poll the JobStatusRestClient for its status or listen on the message queue for updates
        return status;
    }

    /**
     * Build the JobDefinition that will be submitted for execution.
     */
    protected abstract JobDefinition buildJobDefinition(Long notebookId, CellInstance cell);


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
                return new VariableKey(variable.getProducerCell().getName(), variable.getName());
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


    public static class ChemblActivitiesFetcher extends CellJobExecutor {

        protected JobDefinition buildJobDefinition(Long notebookId, CellInstance cell) {

            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_CHEMBL_ACTIVITIES_FETCHER)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, CellRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(notebookId, cell, step1);
        }
    }

    public static class DatasetMerger extends CellJobExecutor {

        protected JobDefinition buildJobDefinition(Long notebookId, CellInstance cell) {

            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_DATASET_MERGER)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, CellRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cell));

            for (int i = 1; i <= 5; i++) {
                VariableKey key = createVariableKey(cell, "input" + i);
                if (key != null) {
                    step1.withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET + i, key);
                } else {
                    break;
                }
            }
            return buildJobDefinition(notebookId, cell, step1);
        }
    }

    public static class BasicObjectToMoleculeObjectConvertor extends CellJobExecutor {

        protected JobDefinition buildJobDefinition(Long notebookId, CellInstance cell) {
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_BASICOBJECT_TO_MOLECULEOBJECT)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, createVariableKeyRequired(cell, CellRegistry.VAR_NAME_INPUT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, CellRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(notebookId, cell, step1);
        }
    }

    public static class CSVFileUploader extends CellJobExecutor {

        protected JobDefinition buildJobDefinition(Long notebookId, CellInstance cell) {

            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_CSV_READER)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, new VariableKey(cell.getName(), CellRegistry.VAR_NAME_FILECONTENT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, CellRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(notebookId, cell, step1);
        }
    }

    public static class SDFileUploader extends CellJobExecutor {

        protected JobDefinition buildJobDefinition(Long notebookId, CellInstance cell) {

            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_SDF_READER)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, new VariableKey(cell.getName(), CellRegistry.VAR_NAME_FILECONTENT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, CellRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(notebookId, cell, step1);
        }
    }

    public static class ValueTransformer extends CellJobExecutor {

        protected JobDefinition buildJobDefinition(Long notebookId, CellInstance cell) {

            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_VALUE_TRANSFORMER)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, createVariableKeyRequired(cell, CellRegistry.VAR_NAME_INPUT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, CellRegistry.VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(notebookId, cell, step1);
        }
    }

    public static class GroovyScriptTrusted extends CellJobExecutor {

        protected JobDefinition buildJobDefinition(Long notebookId, CellInstance cell) {

            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_TRUSTED_GROOVY_DATASET_SCRIPT)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, createVariableKeyRequired(cell, CellRegistry.VAR_NAME_INPUT))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, CellRegistry.VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(notebookId, cell, step1);
        }
    }
}