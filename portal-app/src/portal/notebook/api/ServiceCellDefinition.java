package portal.notebook.api;

import org.squonk.jobdef.JobDefinition;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
@XmlRootElement
public class ServiceCellDefinition extends CellDefinition {

    public static final String OPT_SERVICE_ENDPOINT = StepDefinitionConstants.OPTION_SERVICE_ENDPOINT;
    private final static long serialVersionUID = 1l;
    private static final String SERVICE_ICON = "default_icon.png";
    private static final Logger LOG = Logger.getLogger(ServiceCellDefinition.class.getName());
    private ServiceDescriptor serviceDescriptor;
    private static final String OPTION_BODY = StepDefinitionConstants.OPTION_BODY;

    public ServiceCellDefinition(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
        setExecutable(Boolean.TRUE);
        if (findOptionDescriptorForBody() == null) {
            // if one of the options is defined as the body then we don't want an input endpoint
            getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, determineVariableType(serviceDescriptor.getInputType(), serviceDescriptor.getInputClass())));
        }
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, determineVariableType(serviceDescriptor.getOutputType(), serviceDescriptor.getOutputClass())));
        LOG.info("Creating service cell " + serviceDescriptor.getName() + " with icon " + serviceDescriptor.getIcon());
    }

    private VariableType determineVariableType(ServiceDescriptor.DataType dataType, Class cls) {
        if (dataType == ServiceDescriptor.DataType.STREAM) {
            if (cls == MoleculeObject.class) {
                return VariableType.DATASET_MOLS;
            } else if (cls == BasicObject.class) {
                return VariableType.DATASET_BASIC;
            }
        }

        // this shouldn't happen
        return VariableType.DATASET_ANY;
    }

    private OptionDescriptor findOptionDescriptorForBody() {
        if (serviceDescriptor.getOptions() != null) {
            for (OptionDescriptor od : serviceDescriptor.getOptions()) {
                if (OPTION_BODY.equalsIgnoreCase(od.getKey())) {
                    return od;
                }
            }
        }
        return null;
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

    @Override
    public String[] getTags() {
        return serviceDescriptor.getTags();
    }

    @Override
    public String getIcon() {
        return serviceDescriptor.getIcon();
    }

    class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {


            LOG.info("Building JobDefinition for service " + serviceDescriptor.getExecutionEndpoint());

            StepDefinition step = new StepDefinition(serviceDescriptor.getExecutorClassName())
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, VAR_NAME_OUTPUT)
                    .withOption(OPT_SERVICE_ENDPOINT, serviceDescriptor.getExecutionEndpoint());

            Map<String, Object> options = collectAllOptions(cell);

            OptionDescriptor<?> optDesc = findOptionDescriptorForBody();
            LOG.info("Body OptionDescriptor: " + optDesc);
            if (optDesc != null) {
                Object body = options.remove(OPTION_BODY);
                LOG.info("Setting body option: " + body);
                step = step.withOption(OPTION_BODY, body);
            } else {
                // only define an input binding if one of the options is not specified as the body
                VariableKey key = createVariableKey(cell, VAR_NAME_INPUT);
                if (key != null) {
                    LOG.info("Using input variable " + key.getCellId() + ":" + key.getVariableName() + " as variable " + VAR_NAME_INPUT);
                } else {
                    LOG.info("Input variable " + VAR_NAME_INPUT + " not found");
                }
                step = step.withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, key);
            }

            for (Map.Entry<String, Object> e : options.entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                if (value != null) {
                    LOG.info("Writing option: " + key + " [" + value.getClass().getName() + "] -> " + value);
                    step.withOption(key, value);
                }
            }

            return buildJobDefinition(cellExecutionData, cell, step);
        }

    }
}
