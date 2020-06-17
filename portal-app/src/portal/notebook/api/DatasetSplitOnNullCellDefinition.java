package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.BasicObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetSplitOnNullCellDefinition extends CellDefinition {

    public static final String CELL_NAME = "Split on missing values";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(DatasetSplitOnNullCellDefinition.class.getName());
    private static final String OPTION_FIELD = StepDefinitionConstants.DatasetSplitOnNull.OPTION_FIELD;
    private IODescriptor[] inputs = IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET);
    private IODescriptor[] outputs = new IODescriptor[] {
            IODescriptors.createBasicObjectDataset(StepDefinitionConstants.VARIABLE_OUTPUT_PASS),
            IODescriptors.createBasicObjectDataset(StepDefinitionConstants.VARIABLE_OUTPUT_FAIL)
    };

    public DatasetSplitOnNullCellDefinition() {
        super(CELL_NAME, "Split on missing values", "icons/filter.png", new String[]{"split", "filter", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, Dataset.class, BasicObject.class));
        getVariableDefinitionList().add(outputs[0]);
        getVariableDefinitionList().add(outputs[1]);
        getOptionDefinitionList().add(
                new OptionDescriptor<>(new DatasetFieldTypeDescriptor(null),
                        OPTION_FIELD, "Field to split with",
                        "Name of field whose values are used to split the dataset",
                        Mode.User));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.DatasetSplitOnNull.CLASSNAME, inputs, outputs);
    }

    @Override
    public Class[] getOutputVariableRuntimeType(NotebookInstance notebook, Long cellId, IODescriptor outputDescriptor) {
        return getInputVariableRuntimeType(notebook, cellId, VAR_NAME_INPUT);
    }

}
