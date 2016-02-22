package portal.notebook.cells;

import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.types.MoleculeObject;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;

import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class ServiceCellDefinition extends CellDefinition {

    private static final Logger LOG = Logger.getLogger(ServiceCellDefinition.class.getName());

    public static final String OPT_SERVICE_ENDPOINT = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_ENDPOINT;
    public static final String OPT_SERVICE_PARAMS = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_PARAMS;
    public static final String OPT_SERVICE_PRESERVE_STRUCTURE = StepDefinitionConstants.ServiceExecutor.OPTION_PRESERVE_STRUCTURE;


    public static final ServiceDescriptor[] MOCK_SERVICE_DESCRIPTORS = new ServiceDescriptor[] {
            new ServiceDescriptor(
                    "mock.nooptions", // key
                    "Simple no options", // name
                    "Simple no options", // description
                    new String[]{"tag1", "tag2", "tag3"}, // tags for searching
                    null, // resource URL e.g. wiki page taht describes the service
                    new String[]{"path/to/find", "road/to/noware"}, // path for location in folders. Do we need this?
                    "User1 <user1@squonk.it>", // owner
                    null, // owner's URL
                    new String[]{"public"}, // layers it appears on
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    Metadata.Type.STREAM, // inputTypes
                    Metadata.Type.STREAM, // outputTypes
                    new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "http://www.somewhere.com/logp", // endpoint
                                    false, // URL is relative
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    null, null, null, null, null, null)
                    }
            ),
            new ServiceDescriptor(
                    "mock.clustering",
                    "Mock clustering",
                    "Mock clustering with 2 integer options",
                    new String[]{"tag2", "tag4", "tag6"},
                    null,
                    new String[]{"path/to/find", "road/to/noware"},
                    "User1 <user1@squonk.it>",
                    null,
                    new String[]{"public"},
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    Metadata.Type.STREAM, // inputTypes
                    Metadata.Type.STREAM, // outputTypes
                    new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "http://www.somewhere.com/clustering", // endpoint
                                    false, // URL is relative
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    null, null, null, null, new OptionDescriptor[]{
                                    new OptionDescriptor<>(Integer.class, "header.min_clusters", "Min clusters", "Minimum number of clusters to generate").withDefaultValue(5),
                                    new OptionDescriptor<>(Integer.class, "header.max_clusters", "Max clusters", "Maximum number of clusters to generate").withDefaultValue(10)
                            }, null)
                    }
            ),
            new ServiceDescriptor(
                    "mock.screening",
                    "Mock screening",
                    "Mock screening with molecule and float and pick list options",
                    new String[]{"tag1", "tag3", "tag5"},
                    null,
                    new String[]{"path/to/find", "road/to/noware"},
                    "User1 <user1@squonk.it>",
                    null,
                    new String[]{"public"},
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    Metadata.Type.STREAM, // inputTypes
                    Metadata.Type.STREAM, // outputTypes
                    new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "http://www.somewhere.com/screening", // endpoint
                                    false, // URL is relative
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    null, null, null, null, new OptionDescriptor[]{
                                    new OptionDescriptor<>(new MoleculeTypeDescriptor<>(MoleculeTypeDescriptor.MoleculeType.DISCRETE), "header.query_structure", "Query Structure", "Structure to us as the query"),
                                    new OptionDescriptor<>(Float.class, "header.threshold", "Similarity Cuttoff", "Similarity score cuttoff between 0 and 1 (1 means identical)")
                                            .withDefaultValue(0.7f),
                                    new OptionDescriptor<>(String.class, "header.descriptor", "descriptor", "Molecular descriptor")
                                            .withValues(new String[]{"Chemical hashed fingerprint", "ECFP4"})
                                            .withDefaultValue("ECFP4")
                            }, null)
                    }
            )
    };

    private ServiceDescriptor serviceDescriptor;

    public ServiceCellDefinition(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
        setExecutable(Boolean.TRUE);
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    @Override
    public String getName() {
        return serviceDescriptor.getName();
    }

    @Override
    public String getDescription() {
        return serviceDescriptor.getDescription();
    }

    @Override
    public Boolean getExecutable() {
        return true;
    }


    class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            LOG.info("Building JobDefinition for service " + serviceDescriptor.getAccessModes()[0].getExecutionEndpoint());

            NotebookInstance notebook = cellExecutionData.getNotebookInstance();
            CellInstance cell = notebook.findCellById(cellExecutionData.getCellId());
            VariableKey key = createVariableKey(notebook, cell, VAR_NAME_INPUT);

            // TODO - the step type will need to be defined at the ServiceDescriptor level. For now we use MoleculeServiceThinExecutorStep for everything
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.ServiceExecutor.CLASSNAME)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, key)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOption(OPT_SERVICE_ENDPOINT, serviceDescriptor.getAccessModes()[0].getExecutionEndpoint())
                    .withOption(OPT_SERVICE_PRESERVE_STRUCTURE, true) // TODO - this will need to be defined at the ServiceDescriptor level
                    .withOption(OPT_SERVICE_PARAMS, collectAllOptions(cell));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cell, step1);
        }
    }
}
