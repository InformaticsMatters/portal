package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;
import org.squonk.execution.steps.StepDefinitionConstants.ChemblActivitiesFetcher;

/**
 * Created by timbo on 29/01/16.
 */
public class ChemblActivitiesFetcherCellDefinition extends CellDefinition {

    public static final String CELL_NAME = "ChemblActivitiesFetcher";

    public ChemblActivitiesFetcherCellDefinition() {
        setName(CELL_NAME);
        setDescription("ChEMBL activities fetcher");
        setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName(VAR_DISPLAYNAME_OUTPUT);
        variableDefinition.setVariableType(VariableType.DATASET);
        getOutputVariableDefinitionList().add(variableDefinition);
        getOptionDefinitionList().add(new OptionDescriptor<String>(String.class, ChemblActivitiesFetcher.OPTION_ASSAY_ID, "Assay ID", "ChEMBL Asssay ID"));
        getOptionDefinitionList().add(new OptionDescriptor<String>(String.class, ChemblActivitiesFetcher.OPTION_PREFIX, "Prefix", "Prefix for result fields"));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override

        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {
            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.ChemblActivitiesFetcher.CLASSNAME)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cellInstance));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }

}
