package portal.notebook.api;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class TransformValuesCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "TransformValues";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(TransformValuesCellDefinition.class.getName());
    private static final String OPT_TRANSFORMS = StepDefinitionConstants.ValueTransformer.OPTION_TRANSFORMS;

    public TransformValuesCellDefinition() {
        super(CELL_NAME, "Transform dataset values", "icons/transform_basic.png", new String[]{"transform", "convert", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET_ANY));
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET_ANY));
        getOptionDefinitionList().add(new OptionDescriptor<>(new MultiLineTextTypeDescriptor(10, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                OPT_TRANSFORMS, "Transform Definitions",
                "Definition of the transforms to perform", Mode.User));

        setInitialWidth(400);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.ValueTransformer.CLASSNAME);
    }

}
