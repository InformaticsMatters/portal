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
        super(CELL_NAME, "Merge datasets into one");
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, DatasetMerger.OPTION_MERGE_FIELD_NAME, "Merge field name", "Name of value field which identifies equivalent entries"));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, DatasetMerger.OPTION_KEEP_FIRST, "Prefix", "Prefix for result fields"));
        for (int i = 0; i < 5; i++) {
            getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT + (i + 1), "Input dataset " + (i + 1), VariableType.DATASET));
        }
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            NotebookInstance notebook = cellExecutionData.getNotebookInstance();
            CellInstance cell = notebook.findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.DatasetMerger.CLASSNAME)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cell));

            for (int i = 1; i <= 5; i++) {
                VariableKey key = createVariableKey(notebook, cell, "input" + i);
                if (key != null) {
                    step1.withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET + i, key);
                } else {
                    break;
                }
            }
            return buildJobDefinition(cellExecutionData.getNotebookId(), cell, step1);
        }
    }

}
