package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetMoleculesFromFieldCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "MoleculesFromField";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(DatasetMoleculesFromFieldCellDefinition.class.getName());
    private static final String OPTION_MOLECULES_FIELD = StepDefinitionConstants.DatasetMoleculesFromFieldStep.OPTION_MOLECULES_FIELD;


    public DatasetMoleculesFromFieldCellDefinition() {
        super(CELL_NAME, "New datset from molecules from a field", "icons/molecule_generator.png", new String[]{"molecule", "extractor", "flatmap"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, Dataset.class, BasicObject.class));
        getVariableDefinitionList().add(IODescriptors.createMoleculeObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(new DatasetFieldTypeDescriptor(new Class[] {MoleculeObject[].class}),
                OPTION_MOLECULES_FIELD, "Field with molecules", "Field that contains an Array of MoleculeObjects", Mode.User)
                .withMinMaxValues(1,1));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.DatasetMoleculesFromFieldStep.CLASSNAME);
    }

}
