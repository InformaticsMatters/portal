package portal.notebook.api;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.CellExecutorJobDefinition;
import org.squonk.notebook.api.VariableKey;

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
    protected CellExecutorJobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

        VariableKey key = createVariableKey(cell, "input");
        IODescriptor[] inputs = IODescriptors.createMoleculeObjectDatasetArray("input");
        IODescriptor[] outputs = IODescriptors.createMoleculeObjectDatasetArray("output");

        StepDefinition step = new StepDefinition(stepClassName)
                .withInputs(inputs)
                .withOutputs(outputs)
                .withInputVariableMapping("input", key);

        handleOptions(step, cell);

        return buildJobDefinition(cellExecutionData, cell, inputs, outputs, step);
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
