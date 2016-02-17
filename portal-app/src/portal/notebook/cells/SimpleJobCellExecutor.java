package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellExecutionData;
import portal.notebook.api.CellInstance;
import portal.notebook.api.NotebookInstance;

/**
 * Highly opinionated cell executed that expects a single input and out datasets and used all the default names, and does
 * not need special treatment of options.
 */
class SimpleJobCellExecutor extends AbstractJobCellExecutor {

    private final String stepClassName;

    SimpleJobCellExecutor( String stepClassName) {
        this.stepClassName = stepClassName;
    }


    @Override
    protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {
        NotebookInstance notebook = cellExecutionData.getNotebookInstance();
        CellInstance cell = notebook.findCellById(cellExecutionData.getCellId());
        VariableKey key = createVariableKey(notebook, cell, CellDefinition.VAR_NAME_INPUT);

        StepDefinition step = new StepDefinition(stepClassName)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, key)
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                .withOptions(collectAllOptions(cell));

        return buildJobDefinition(cellExecutionData.getNotebookId(), cell, step);
    }
}