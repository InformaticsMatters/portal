package portal.notebook.execution.service;

import com.im.lac.job.client.JobStatusRestClient;
import com.im.lac.job.jobdef.*;
import org.squonk.client.JobStatusClient;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.api.VariableKey;
import portal.notebook.CellModel;
import portal.notebook.NotebookSession;
import portal.notebook.service.Cell;
import portal.notebook.service.Option;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by timbo on 16/01/16.
 */
public class ChemblCellExecute {

    // This would be an abstract method to be overridden by each cell definition.
    // For now this serves as an example of how it it would look
    public JobStatus execute(Long notebookId, CellModel cell) throws Exception {

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

    JobDefinition buildJobDefinition(Long notebookId, CellModel cell) {
        String cellName = cell.getName(); // get the cell name from somewhere

        // Get the options as a simple name/value map
        Map<String,Object> options = cell.getOptionModelMap().entrySet().stream().collect(
                Collectors.toMap(e -> e.getKey(), e -> e.getValue().getValue()));

        // get the name for the output variable. Just one in this case.
        //String outputVarName = cell.getOutputVariableModelMap().get("results").getName();

        // build the step definition(s). If there is only one step then this is easy.
        // if multiple steps then the cell will know how to manage the bindings and options
        StepDefinition step1 = new StepDefinition("org.squonk.execution.steps.impl.ChemblActivitiesFetcherStep")
                //.withInputVariableMapping("cellsKey", inputBindingMappings.get("nbvariable") ) // this cell has no inputs, but others do
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "results")
                .withOptions(options);

        // create the job definition
        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition();
        jobdef.configureCellAndSteps(notebookId, cellName, step1);

        return jobdef;
    }

}
