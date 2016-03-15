package portal.notebook.cells;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;

import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
public class TransformValuesCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "TransformValues";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(TransformValuesCellDefinition.class.getName());
    private static final String OPT_TRANSFORMS = StepDefinitionConstants.ValueTransformer.OPTION_TRANSFORMS;

    public TransformValuesCellDefinition() {
        super(CELL_NAME, "Transform dataset values", "icons/transform.png", new String[]{"transform", "convert", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<>(new MultiLineTextTypeDescriptor(10, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                OPT_TRANSFORMS, "Transform Definitions",
                "Definition of the transforms to perform"));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.ValueTransformer.CLASSNAME);
    }

}
