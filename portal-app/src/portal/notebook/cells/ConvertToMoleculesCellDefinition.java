package portal.notebook.cells;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;


/**
 * Created by timbo on 29/01/16.
 */
public class ConvertToMoleculesCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "ConvertToMolecules";
    private final static long serialVersionUID = 1l;

    public ConvertToMoleculesCellDefinition() {
        super(CELL_NAME, "Convert to Molecules", "default_icon.png", new String[]{"convert", "transform", "structures", "molecules"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        DatasetFieldOptionDescriptor structureOptionDescriptor = new DatasetFieldOptionDescriptor("structureFieldName", "Structure Field Name",
                "Name of property to use for the structure", "structure");
        getOptionDefinitionList().add(structureOptionDescriptor);
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, "structureFormat",
                "Structure Format", "Format of the structures e.g. smiles, mol")
                .withValues(new String[]{"smiles", "mol"}));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, "preserveUuid", "Preserve UUID", "Keep the existing UUID or generate a new one")
                .withMinValues(1)
                .withDefaultValue(true));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.ConvertBasicToMoleculeObject.CLASSNAME);
    }

}
