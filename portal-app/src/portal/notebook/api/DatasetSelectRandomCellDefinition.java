package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.BasicObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetSelectRandomCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "Dataset random selector";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(DatasetSelectRandomCellDefinition.class.getName());
    private static final String OPTION_RANDOM = StepDefinitionConstants.DatasetSelectRandom.OPTION_RANDOM;
    private static final String OPTION_COUNT = StepDefinitionConstants.DatasetSelectRandom.OPTION_COUNT;

    public DatasetSelectRandomCellDefinition() {
        super(CELL_NAME, "Generate a slice of the dataset", "icons/filter.png", new String[]{"filter", "random", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, Dataset.class, BasicObject.class));
        getVariableDefinitionList().add(IODescriptors.createBasicObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(Float.class, OPTION_RANDOM, "Random fraction", "The fraction or records to randomly select (between 0 and 1, default 0.001)", Mode.User));
        getOptionDefinitionList().add(new OptionDescriptor<>(Integer.class, OPTION_COUNT, "Max records", "The max number of records to include, default 1000", Mode.User));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.DatasetSelectRandom.CLASSNAME);
    }

    @Override
    public Class[] getOutputVariableRuntimeType(NotebookInstance notebook, Long cellId, IODescriptor outputDescriptor) {
        return getInputVariableRuntimeType(notebook, cellId, VAR_NAME_INPUT);
    }

}
