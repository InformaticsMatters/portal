package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.DatasetMerger;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;


/**
 * Created by timbo on 29/01/16.
 */
public class ConvertToMoleculesCellDefinition extends CellDefinition {

    public static final String CELL_NAME = "ConvertToMolecules";

    public ConvertToMoleculesCellDefinition() {
        super(CELL_NAME, "Convert to Molecules");
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, "structureFieldName", "Structure Field Name",
                "Name of property to use for the structure").withDefaultValue("structure"));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, "structureFormat",
                "Structure Format", "Format of the structures e.g. smiles, mol")
                .withValues(new String[]{"smiles", "mol"}));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, "preserveUuid", "Preserve UUID", "Keep the existing UUID or generate a new one").withMinValues(1));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.ConvertBasicToMoleculeObject.CLASSNAME);
    }

}
