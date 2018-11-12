package portal.notebook.api;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.jobdef.JobDefinition;
import org.squonk.notebook.api.VariableKey;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Highly opinionated cell executer that expects a no inputs and one output dataset name output, and does
 * not need special treatment of options.
 */
@XmlRootElement
class ZeroInputOneOutputJobCellExecutor extends AbstractJobCellExecutor {

    private final String stepClassName;

    ZeroInputOneOutputJobCellExecutor(String stepClassName) {
        this.stepClassName = stepClassName;
    }


    @Override
    protected JobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

        VariableKey key = createVariableKey(cell, "input");
        IODescriptor[] outputs = IODescriptors.createMoleculeObjectDatasetArray("output");

        StepDefinition step = new StepDefinition(stepClassName).withOutputs(outputs);

        handleOptions(step, cell);

        return buildJobDefinition(cellExecutionData, cell, null, outputs, step);
    }

    /** Hook to allow option handling to be customised.
     * Default is to add all the specified options as they are.
     *
     * @param step
     * @param cell
     */
    protected void handleOptions(StepDefinition step, CellInstance cell) {
        step.withOptions(collectAllOptions(cell));
    }

}
