package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.BasicObject;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class ConvertToMoleculesCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "ConvertToMolecules";
    private final static long serialVersionUID = 1l;

    public ConvertToMoleculesCellDefinition() {
        super(CELL_NAME, "Convert to Molecules", "icons/transform_basic_to_molecule.png", new String[]{"convert", "transform", "structures", "molecules"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, Dataset.class, BasicObject.class));
        getVariableDefinitionList().add(IODescriptors.createMoleculeObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(new DatasetFieldTypeDescriptor(new Class[] {String.class}),
                "structureFieldName", "Structure Field Name", "Name of property to use for the structure", Mode.User));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, "structureFormat",
                "Structure Format", "Format of the structures e.g. smiles, mol", Mode.User)
                .withValues(new String[]{"smiles", "mol"}));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, "preserveUuid", "Preserve UUID", "Keep the existing UUID or generate a new one", Mode.User)
                .withMinMaxValues(1,1)
                .withDefaultValue(true));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.ConvertBasicToMoleculeObject.CLASSNAME);
    }

}
