package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.BasicObject;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class ProcessDatasetUntrustedPythonScriptCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "UntrustedPythonDatasetScript";
    private final static long serialVersionUID = 1l;


    public ProcessDatasetUntrustedPythonScriptCellDefinition() {
        super(CELL_NAME, "Python Script (untrusted)", "icons/program.png", new String[]{"script", "python", "dataset", "docker", "programming"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, Dataset.class, BasicObject.class));
        getVariableDefinitionList().add(IODescriptors.createBasicObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(
                new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "script", "Python Script", "Python script to execute", Mode.User));

        setInitialWidth(400);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.UntrustedPythonDatasetScript.CLASSNAME);
    }


}
