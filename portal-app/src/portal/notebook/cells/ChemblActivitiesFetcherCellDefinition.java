package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import portal.notebook.api.*;
import static org.squonk.execution.steps.StepDefinitionConstants.ChemblActivitiesFetcher.*;

/**
 * Created by timbo on 29/01/16.
 */
public class ChemblActivitiesFetcherCellDefinition extends CellDefinition {

    public ChemblActivitiesFetcherCellDefinition() {
        setName("ChemblActivitiesFetcher");
        setDescription("ChEMBL activities fetcher");
        setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName(VAR_DISPLAYNAME_OUTPUT);
        variableDefinition.setVariableType(VariableType.DATASET);
        getOutputVariableDefinitionList().add(variableDefinition);
        getOptionDefinitionList().add(new OptionDefinition(String.class, OPTION_ASSAY_ID, "Assay ID", "ChEBML Asssay ID"));
        getOptionDefinitionList().add(new OptionDefinition(String.class, OPTION_PREFIX, "Prefix", "Prefix for result fields"));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(Long notebookId, CellInstance cell) {
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.ChemblActivitiesFetcher.CLASSNAME)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(notebookId, cell, step1);
        }
    }

}
