package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
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
public class DatasetSorterCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "Dataset sorter";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(DatasetSorterCellDefinition.class.getName());
    private static final String OPTION_DIRECTIVES = StepDefinitionConstants.DatasetSorter.OPTION_DIRECTIVES;

    public DatasetSorterCellDefinition() {
        super(CELL_NAME, "Sort the dataset", "icons/filter.png", new String[]{"sort", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, Dataset.class, BasicObject.class));
        getVariableDefinitionList().add(IODescriptors.createBasicObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(new MultiLineTextTypeDescriptor(10, 80, MultiLineTextTypeDescriptor.MIME_TYPE_TEXT_PLAIN),
                OPTION_DIRECTIVES, "Sort directives",
                "Definition of the sort directives: field_name ASC|DESC", Mode.User));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.DatasetSorter.CLASSNAME);
    }

    @Override
    public Class[] getOutputVariableRuntimeType(NotebookInstance notebook, Long cellId, IODescriptor outputDescriptor) {
        return getInputVariableRuntimeType(notebook, cellId, VAR_NAME_INPUT);
    }

}
