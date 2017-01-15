package portal.notebook.api;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetSelectSliceCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "Dataset slice selector";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(DatasetSelectSliceCellDefinition.class.getName());
    private static final String OPTION_SKIP = StepDefinitionConstants.DatasetSelectSlice.OPTION_SKIP;
    private static final String OPTION_COUNT = StepDefinitionConstants.DatasetSelectSlice.OPTION_COUNT;

    public DatasetSelectSliceCellDefinition() {
        super(CELL_NAME, "Generate a slice of the dataset", "icons/filter.png", new String[]{"filter", "slice", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition("input", VariableType.DATASET_ANY));
        getVariableDefinitionList().add(new VariableDefinition("output", VariableType.DATASET_ANY));
        getOptionDefinitionList().add(new OptionDescriptor<>(Integer.class, OPTION_SKIP, "Number to skip", "The number of records to skip", Mode.User));
        getOptionDefinitionList().add(new OptionDescriptor<>(Integer.class, OPTION_COUNT, "Number to include", "The number of records to include after skipping", Mode.User));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.DatasetSelectSlice.CLASSNAME);
    }

}
