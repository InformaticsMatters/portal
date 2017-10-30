package portal.notebook.api;

import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.jobdef.JobDefinition;
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
    private ServiceConfig serviceConfig;
    private static final String OPTION_BODY = StepDefinitionConstants.OPTION_BODY;

    public ServiceCellDefinition(ServiceConfig serviceConfig) {
        LOG.info("Creating service cell " + serviceConfig.getName() + " with icon " + serviceConfig.getIcon());
        this.serviceConfig = serviceConfig;
        setExecutable(Boolean.TRUE);
        OptionDescriptor[] options = serviceConfig.getOptionDescriptors();
        if (options != null) {
            LOG.fine(options.length + " options found for service " + serviceConfig.getName());
            for (OptionDescriptor option : options) {
                LOG.finer("option: " + option.getLabel() + " type: " + option.getTypeDescriptor().getType());
                getOptionDefinitionList().add(option);
            }
        }

        // TODO - this check should be unnecessary once all services are defined correctly
        // if one of the options is defined as the body then we don't want an input endpoint
        if (findOptionDescriptorForBody() == null) {
            if (serviceConfig.getInputDescriptors() != null) {
                for (IODescriptor input : serviceConfig.getInputDescriptors()) {
                    getBindingDefinitionList().add(new BindingDefinition(input.getName(), input.getPrimaryType(), input.getSecondaryType()));
                }
            }
        }

        IODescriptor[] outputs = serviceConfig.getOutputDescriptors();
        if (outputs != null && outputs.length > 0) {
            for (IODescriptor output : serviceConfig.getOutputDescriptors()) {
                getVariableDefinitionList().add(output);
            }
        }
    }


    private OptionDescriptor findOptionDescriptorForBody() {
        if (serviceConfig.getOptionDescriptors() != null) {
            for (OptionDescriptor od : serviceConfig.getOptionDescriptors()) {
                if (OPTION_BODY.equalsIgnoreCase(od.getKey())) {
                    return od;
                }
            }
        }
        return null;
    }

    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    @Override
    public String getName() {
        return serviceConfig.getName();
    }

    @Override
    public String getDescription() {
        return serviceConfig.getDescription();
    }

    @Override
    public Boolean getExecutable() {
        return true;
    }

    @Override
    public String[] getTags() {
        return serviceConfig.getTags();
    }

    @Override
    public String getIcon() {
        return serviceConfig.getIcon();
    }

    class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

            LOG.info("Building JobDefinition for service " + serviceConfig.getId());

            StepDefinition step = new StepDefinition(serviceConfig.getExecutorClassName(), serviceConfig.getId());
            step.withInputs(serviceConfig.getInputDescriptors())
                    .withOutputs(serviceConfig.getOutputDescriptors());

            Map<String, Object> options = collectAllOptions(cell);

            OptionDescriptor<?> optDesc = findOptionDescriptorForBody();
            LOG.fine("Body OptionDescriptor: " + optDesc);
            if (optDesc != null) {
                Object body = options.remove(OPTION_BODY);
                LOG.fine("Setting body option: " + body);
                step = step.withOption(OPTION_BODY, body);
            } else {
                if (serviceConfig.getInputDescriptors() != null) {
                    for (IODescriptor iod : serviceConfig.getInputDescriptors()) {
                        VariableKey key = createVariableKey(cell, iod.getName());
                        if (key != null) {
                            LOG.fine("Using input variable " + key.getCellId() + ":" + key.getVariableName() + " as variable " + iod.getName());
                        } else {
                            LOG.info("Input variable " + iod.getName() + " not found");
                        }
                        step.withInputVariableMapping(iod.getName(), key);
                    }
                }
            }

            for (Map.Entry<String, Object> e : options.entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                if (value != null) {
                    LOG.fine("Writing option: " + key + " [" + value.getClass().getName() + "] -> " + value);
                    step.withOption(key, value);
                }
            }

            return buildJobDefinition(cellExecutionData, cell, serviceConfig.getInputDescriptors(), serviceConfig.getOutputDescriptors(), step);
        }

    }
}
