package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;

/**
 * Created by timbo on 29/01/16.
 */
public class ChemblActivitiesFetcherCellDefinition extends CellDefinition {
    public static final long serialVersionUID = 1l;

    public static final String CELL_NAME = "ChemblActivitiesFetcher";
    public static final String OPT_ASSAY_ID = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_ASSAY_ID;
    public static final String OPT_PREFIX = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_PREFIX;

    public ChemblActivitiesFetcherCellDefinition() {
        super(CELL_NAME, "ChEMBL activities fetcher", new String[] {"chembl", "assay", "rest"});
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPT_ASSAY_ID, "Assay ID", "ChEMBL Asssay ID"));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPT_PREFIX, "Prefix", "Prefix for result fields"));
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
