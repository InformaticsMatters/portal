package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import portal.notebook.api.*;
import portal.notebook.execution.service.CellRegistry;

/**
 * Created by timbo on 29/01/16.
 */
public class ChemblActivitiesFetcherCell extends CellDefinition {

    public ChemblActivitiesFetcherCell() {
        setName("ChemblActivitiesFetcher");
        setDescription("Chembl activities fetcher");
        setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        getOutputVariableDefinitionList().add(variableDefinition);
        getOptionDefinitionList().add(new OptionDefinition(String.class, "assayId", "Assay ID", "ChEBML Asssay ID"));
        getOptionDefinitionList().add(new OptionDefinition(String.class, "prefix", "Prefix", "Prefix for result fields"));
        setCellExecutor(new Executor());
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(Long notebookId, CellInstance cell) {
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_CHEMBL_ACTIVITIES_FETCHER)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, CellRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(notebookId, cell, step1);
        }
    }

}
