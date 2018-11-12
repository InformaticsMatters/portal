package portal.notebook.api;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.CellExecutorJobDefinition;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class ChemblActivitiesFetcherCellDefinition extends CellDefinition {

    public static final long serialVersionUID = 1L;
    public static final String CELL_NAME = "ChemblActivitiesFetcher";
    public static final String OPT_ASSAY_ID = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_ASSAY_ID;
    public static final String OPT_PREFIX = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_PREFIX;

    public ChemblActivitiesFetcherCellDefinition() {
        super(CELL_NAME, "ChEMBL activities fetcher", "icons/import_external_service.png", new String[]{"chembl", "assay", "rest"});
        getVariableDefinitionList().add(IODescriptors.createMoleculeObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPT_ASSAY_ID, "Assay ID", "ChEMBL Asssay ID", Mode.User));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPT_PREFIX, "Prefix", "Prefix for result fields", Mode.User));

    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected CellExecutorJobDefinition buildJobDefinition(CellInstance cellInstance, CellExecutionData cellExecutionData) {

            IODescriptor[] outputs = IODescriptors.createMoleculeObjectDatasetArray(VAR_NAME_OUTPUT);

            Map<String,Object> options = collectAllOptions(cellInstance);
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.ChemblActivitiesFetcher.CLASSNAME)
                    .withOutputs(outputs)
                    .withOptions(options);

            return buildJobDefinition(cellExecutionData, cellInstance,
                    null, // inputs
                    outputs, // outputs
                    step1);
        }
    }
}
