package portal.notebook.cells;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;


/**
 * Created by timbo on 29/01/16.
 */
public class ProcessDatasetUntrustedGroovyScriptCellDefinition extends CellDefinition {

    public static final String CELL_NAME = "UntrustedGroovyDatasetScript";


    public ProcessDatasetUntrustedGroovyScriptCellDefinition() {
        super(CELL_NAME, "Groovy Script (untrusted)", new String[] {"script", "groovy"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<>(
                new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "script", "Groovy Script", "Groovy script to execute"));
    }

    @Override
    public CellExecutor getCellExecutor() {
        //return new SimpleJobCellExecutor(StepDefinitionConstants.UntrustedGroovyDataset.CLASSNAME);
        return new SimpleJobCellExecutor("org.squonk.execution.steps.impl.UntrustedGroovyDatasetScriptStep");
    }

}
