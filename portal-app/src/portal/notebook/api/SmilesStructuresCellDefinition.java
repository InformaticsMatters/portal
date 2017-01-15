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
public class SmilesStructuresCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "SmilesStructures";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(SmilesStructuresCellDefinition.class.getName());
    private static final String OPTION_SMILES = StepDefinitionConstants.SmilesStructures.OPTION_SMILES;

    public SmilesStructuresCellDefinition() {
        super(CELL_NAME, "Read structures from smiles", "icons/molecules.png", new String[]{"smiles", "dataset"});
        getVariableDefinitionList().add(new VariableDefinition("output", VariableType.DATASET_MOLS));
        getOptionDefinitionList().add(new OptionDescriptor<>(new MultiLineTextTypeDescriptor(10, 80, MultiLineTextTypeDescriptor.MIME_TYPE_TEXT_PLAIN),
                OPTION_SMILES, "Smiles",
                "Smiles as text, with optional name", Mode.User));

        setInitialWidth(400);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.SmilesStructures.CLASSNAME);
    }

}
