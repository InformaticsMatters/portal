package portal.notebook.api;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
@XmlRootElement
public class ServiceCellDefinition extends CellDefinition {

    public static final String OPT_SERVICE_ENDPOINT = StepDefinitionConstants.MoleculeServiceThinExecutor.OPTION_SERVICE_ENDPOINT;
    public static final String OPT_SERVICE_PARAMS = StepDefinitionConstants.MoleculeServiceThinExecutor.OPTION_SERVICE_PARAMS;
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
            getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        }
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        LOG.info("Creating service cell " + serviceDescriptor.getName() + " with icon " + serviceDescriptor.getIcon());
    }

    private OptionDescriptor findOptionDescriptorForBody() {
        AccessMode accessMode = serviceDescriptor.getAccessModes()[0];
        if (accessMode.getParameters() != null) {
            for (OptionDescriptor od : accessMode.getParameters()) {
                if (OPTION_BODY.equalsIgnoreCase(od.getkey())) {
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

            AccessMode accessMode = serviceDescriptor.getAccessModes()[0];
            LOG.info("Building JobDefinition for service " + accessMode.getExecutionEndpoint());

            Map<String, Object> options = collectAllOptions(cell);
            OptionDescriptor<?> optDesc = findOptionDescriptorForBody();
            LOG.info("Body OptionDescriptor: " + optDesc);
            Object body = null;
            if (optDesc != null) {
                body = options.remove(OPTION_BODY);
            }

            StepDefinition step = new StepDefinition(accessMode.getAdapterClassName())
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, VAR_NAME_OUTPUT)
                    .withOption(OPT_SERVICE_ENDPOINT, serviceDescriptor.getAccessModes()[0].getExecutionEndpoint())
                    .withOption(OPT_SERVICE_PARAMS, options);

            if (optDesc != null) {
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

            return buildJobDefinition(cellExecutionData, cell, step);
        }
    }
}
