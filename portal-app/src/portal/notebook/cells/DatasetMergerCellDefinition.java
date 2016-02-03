package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.DatasetMerger;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;


/**
 * Created by timbo on 29/01/16.
 */
public class DatasetMergerCellDefinition extends CellDefinition {

    public static final String CELL_NAME = "DatasetMerger";

    public DatasetMergerCellDefinition() {
        setName(CELL_NAME);
        setDescription("Merge mulitple datasets into one");
        setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName(VAR_DISPLAYNAME_OUTPUT);
        variableDefinition.setVariableType(VariableType.DATASET);
        getOutputVariableDefinitionList().add(variableDefinition);
        getOptionDefinitionList().add(new OptionDescriptor<String>(String.class, DatasetMerger.OPTION_MERGE_FIELD_NAME, "Merge field name", "Name of value field which identifies equivalent entries"));
        getOptionDefinitionList().add(new OptionDescriptor<String>(String.class, DatasetMerger.OPTION_KEEP_FIRST, "Prefix", "Prefix for result fields"));
        for (int i = 0; i < 5; i++) {
            BindingDefinition bindingDefinition = new BindingDefinition();
            bindingDefinition.setDisplayName("Input dataset " + (i + 1));
            bindingDefinition.setName(VAR_NAME_INPUT + (i + 1));
            bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
            getBindingDefinitionList().add(bindingDefinition);
        }
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.DatasetMerger.CLASSNAME)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
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

}
