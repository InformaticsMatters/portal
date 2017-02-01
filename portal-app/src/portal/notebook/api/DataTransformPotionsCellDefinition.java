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
public class DataTransformPotionsCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "DatasetPotion";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(DataTransformPotionsCellDefinition.class.getName());
    private static final String OPT_POTION = StepDefinitionConstants.DataTransformPotion.OPTION_POTION;

    public DataTransformPotionsCellDefinition() {
        super(CELL_NAME, "Transform dataset values with a potion", "icons/transform_basic.png", new String[]{"potion", "transform", "convert", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, Dataset.class, BasicObject.class));
        getVariableDefinitionList().add(IODescriptors.createBasicObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(new MultiLineTextTypeDescriptor(10, 80, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                OPT_POTION, "Potion Definition",
                "Definition of the transforms to perform", Mode.User));

        setInitialWidth(400);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.DataTransformPotion.CLASSNAME);
    }

    @Override
    public Class[] getOutputVariableRuntimeType(NotebookInstance notebook, Long cellId, IODescriptor outputDescriptor) {
        return getInputVariableRuntimeType(notebook, cellId, VAR_NAME_INPUT);
    }

}
