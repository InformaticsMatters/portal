package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.BasicObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class SimpleSorterCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "Simple sorter";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(SimpleSorterCellDefinition.class.getName());
    private static final String OPTION_FIELD = StepDefinitionConstants.SimpleSorter.OPTION_FIELD;
    private static final String OPTION_ASC = StepDefinitionConstants.SimpleSorter.OPTION_ASC;

    public SimpleSorterCellDefinition() {
        super(CELL_NAME, "Simple sort", "icons/filter.png", new String[]{"sort", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, Dataset.class, BasicObject.class));
        getVariableDefinitionList().add(IODescriptors.createBasicObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(
                new OptionDescriptor<>(new DatasetFieldTypeDescriptor(null),
                        OPTION_FIELD, "Field to sort with",
                        "Name of field whose values are used to sort the dataset",
                        OptionDescriptor.Mode.User));
        getOptionDefinitionList().add(
                new OptionDescriptor<>(Boolean.class, OPTION_ASC, "Sort ascending",
                        "Sort the data in ascending order", OptionDescriptor.Mode.User)
                        .withDefaultValue(Boolean.TRUE));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.SimpleSorter.CLASSNAME);
    }

    @Override
    public Class[] getOutputVariableRuntimeType(NotebookInstance notebook, Long cellId, IODescriptor outputDescriptor) {
        return getInputVariableRuntimeType(notebook, cellId, VAR_NAME_INPUT);
    }

}
