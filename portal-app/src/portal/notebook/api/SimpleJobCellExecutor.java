package portal.notebook.api;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import portal.notebook.webapp.BindingsPanel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Highly opinionated cell executer that expects a single input and out datasets and used all the default names, and does
 * not need special treatment of options.
 */
@XmlRootElement
class SimpleJobCellExecutor extends AbstractJobCellExecutor {

    private final String stepClassName;

    SimpleJobCellExecutor( String stepClassName) {
        this.stepClassName = stepClassName;
    }


    @Override
    protected JobDefinition buildJobDefinition(BindingsPanel.CellInstance cell, CellExecutionData cellExecutionData) {

        VariableKey key = createVariableKey(cell, CellDefinition.VAR_NAME_INPUT);

        StepDefinition step = new StepDefinition(stepClassName)
                .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, key)
                .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT);

        handleOptions(step, cell);

        return buildJobDefinition(cellExecutionData, cell, step);
    }

    /** Hook to allow option handling to be customised.
     * Default is to add all the specified options as they are.
     *
     * @param step
     * @param cell
     */
    protected void handleOptions(StepDefinition step, BindingsPanel.CellInstance cell) {
        step.withOptions(collectAllOptions(cell));
    }

}
