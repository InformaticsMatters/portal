package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.MoleculeObject;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class SmilesDeduplicatorCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "SmilesDeduplicator";
    private final static long serialVersionUID = 1l;


    public SmilesDeduplicatorCellDefinition() {
        super(CELL_NAME, "Deduplicate structures using canonical smiles field", "icons/program_filter.png", new String[]{"smiles", "deduplicate", "duplicate", "filter", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, Dataset.class, MoleculeObject.class));
        getVariableDefinitionList().add(IODescriptors.createMoleculeObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(new DatasetFieldTypeDescriptor(new Class[] {String.class}),
                       StepDefinitionConstants.SmilesDeduplicator.OPTION_CANONICAL_SMILES_FIELD,
                "Canonical smiles field", "Field with canonical smiles that identifies identical structures", Mode.User)
                .withMinMaxValues(1,1));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, StepDefinitionConstants.SmilesDeduplicator.OPTION_KEEP_FIRST_FIELDS,
                "Keep first value fields", "When multiple values keep the first for these fields", Mode.User)
                .withMinValues(0));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, StepDefinitionConstants.SmilesDeduplicator.OPTION_KEEP_LAST_FIELDS,
                "Keep last value fields", "When multiple values keep the last for these fields", Mode.User)
                .withMinValues(0));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, StepDefinitionConstants.SmilesDeduplicator.OPTION_APPEND_FIELDS,
                "Append all values fields", "When multiple values append to list for these fields", Mode.User)
                .withMinValues(0));

    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.SmilesDeduplicator.CLASSNAME);
    }

}
