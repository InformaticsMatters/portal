package portal.notebook.api;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class ProcessDatasetUntrustedGroovyScriptCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "UntrustedGroovyDatasetScript";
    private final static long serialVersionUID = 1l;


    public ProcessDatasetUntrustedGroovyScriptCellDefinition() {
        super(CELL_NAME, "Groovy Script (untrusted)", "icons/program.png", new String[]{"script", "groovy", "dataset", "docker", "programming"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<>(
                new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "script", "Groovy Script", "Groovy script to execute"));
        //setInitialWidth(400);
        //setInitialHeight(275);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.UntrustedGroovyDatasetScript.CLASSNAME);
    }

}
