package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import portal.notebook.api.*;

/**
 * Created by timbo on 29/01/16.
 */
public class ChemblActivitiesFetcherCellDefinition extends CellDefinition {


    public static final String CELL_NAME = "ChemblActivitiesFetcher";
    public static final String CELL_DESCRIPTION = "ChEMBL activities fetcher";
    public static final String OPT_NAME_ASSAY_ID = "assayId";
    public static final String OPT_DISPLAYNAME_ASSAY_ID = "Assay ID";
    public static final String OPT_DESCRIPTION_ASSAY_ID = "ChEBML Asssay ID";
    public static final String OPT_NAME_PREFIX = "prefix";
    public static final String OPT_DISPLAYNAME_PREFIX = "Prefix";
    public static final String OPT_DESCRIPTION_PREFIX = "Prefix for result fields";


    public ChemblActivitiesFetcherCellDefinition() {
        setName(CELL_NAME);
        setDescription(CELL_DESCRIPTION);
        setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName(VAR_DISPLAYNAME_OUTPUT);
        variableDefinition.setVariableType(VariableType.DATASET);
        getOutputVariableDefinitionList().add(variableDefinition);
        getOptionDefinitionList().add(new OptionDefinition(String.class, OPT_NAME_ASSAY_ID, OPT_DISPLAYNAME_ASSAY_ID, OPT_DESCRIPTION_ASSAY_ID));
        getOptionDefinitionList().add(new OptionDefinition(String.class, OPT_NAME_PREFIX, OPT_DISPLAYNAME_PREFIX, OPT_DESCRIPTION_PREFIX));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.STEP_CHEMBL_ACTIVITIES_FETCHER)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_RESULTS)
                    .withOptions(collectAllOptions(cellInstance));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }

}
