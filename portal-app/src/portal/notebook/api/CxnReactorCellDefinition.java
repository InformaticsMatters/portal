package portal.notebook.api;

import com.fasterxml.jackson.core.type.TypeReference;
import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.CxnReactor;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.CellExecutorJobDefinition;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class CxnReactorCellDefinition extends CellDefinition {

    private static final Logger LOG = Logger.getLogger(CxnReactorCellDefinition.class.getName());

    public static final String CELL_NAME = "ChemAxonReactor";
    private final static long serialVersionUID = 1l;

    private static String INPUT_R1 = "Reactant1";
    private static String INPUT_R2 = "Reactant2";


    public CxnReactorCellDefinition() {
        super(CELL_NAME, "Reaction enumeration", "icons/molecule_generator.png", new String[]{"enumeration", "reaction", "library", "dataset"});

        getBindingDefinitionList().add(new BindingDefinition(INPUT_R1, Dataset.class, MoleculeObject.class));
        getBindingDefinitionList().add(new BindingDefinition(INPUT_R2, Dataset.class, MoleculeObject.class));

        getVariableDefinitionList().add(IODescriptors.createMoleculeObjectDataset(VAR_NAME_OUTPUT));

        List<String> rxnnames = getReactionNames();
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, CxnReactor.OPTION_REACTION, "Reaction", "Reaction from the ChemAxon reaction library", Mode.User)
            .withValues(rxnnames.toArray(new String[rxnnames.size()]))
            .withMinMaxValues(1,1));

        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, CxnReactor.OPTION_IGNORE_REACTIVITY, "Ignore reactivity rules", "Ignore reactivity rules when reacting", Mode.User));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, CxnReactor.OPTION_IGNORE_SELECTIVITY, "Ignore selectivity rules", "Ignore selectivity rules when reacting", Mode.User));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, CxnReactor.OPTION_IGNORE_TOLERANCE, "Ignore tolerance rules", "Ignore tolerance rules when reacting", Mode.User));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, "outputFormat", "Output format", "Molecules format for products (smiles or mol)", Mode.User)
                .withValues(new String[]{"smiles", "mol"})
                .withDefaultValue("smiles")
                .withMinMaxValues(1, 1));

    }

    protected List<String> getReactionNames() {
        try {
            // this requires chemservices to be running
            URL url = new URL("http://chemservices:8080/chem-services-chemaxon-basic/rest/v1/reactor/reaction_names");
            List<String> names;
            try (InputStream is = url.openStream()) {
                names = JsonHandler.getInstance().objectFromJson(is, new TypeReference<List<String>>() {});
            }
            LOG.info("Loaded " + names.size() + " reactions");
            return names;
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to load reactions", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected CellExecutorJobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

            IODescriptor[] inputs = new IODescriptor[] {
                    IODescriptors.createMoleculeObjectDataset(INPUT_R1),
                    IODescriptors.createMoleculeObjectDataset(INPUT_R2)
            };
            IODescriptor[] outputs = new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")};

            StepDefinition step1 = new StepDefinition(CxnReactor.CLASSNAME)
                    .withInputs(inputs)
                    .withOutputs(outputs)
                    .withInputVariableMapping(CxnReactor.VARIABLE_R1, createVariableKey(cell, INPUT_R1))
                    .withInputVariableMapping(CxnReactor.VARIABLE_R2, createVariableKey(cell, INPUT_R2))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cell));


            return buildJobDefinition(cellExecutionData, cell, inputs, outputs, step1);
        }
    }

}
