package portal.notebook.api;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import portal.notebook.webapp.BindingsPanel;


import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
@XmlRootElement
public class ServiceCellDefinition extends CellDefinition {

    public static final String OPT_SERVICE_ENDPOINT = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_ENDPOINT;
    public static final String OPT_SERVICE_PARAMS = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_PARAMS;
    public static final String OPT_SERVICE_PRESERVE_STRUCTURE = StepDefinitionConstants.ServiceExecutor.OPTION_PRESERVE_STRUCTURE;
    private final static long serialVersionUID = 1l;
    private static final String SERVICE_ICON = "default_icon.png";
    private static final Logger LOG = Logger.getLogger(ServiceCellDefinition.class.getName());
    private ServiceDescriptor serviceDescriptor;

    public ServiceCellDefinition(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
        setExecutable(Boolean.TRUE);
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        LOG.info("Creating service cell " + serviceDescriptor.getName() + " with icon " + serviceDescriptor.getIcon());
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

            LOG.info("Building JobDefinition for service " + serviceDescriptor.getAccessModes()[0].getExecutionEndpoint());

            VariableKey key = createVariableKey(cell, VAR_NAME_INPUT);
            if (key != null) {
                LOG.info("Using input variable " + key.getCellId() + ":" + key.getVariableName() + " as variable " + VAR_NAME_INPUT);
            } else {
                LOG.info("Input variable " + VAR_NAME_INPUT + " not found");
            }

            // TODO - the step type will need to be defined at the ServiceDescriptor level. For now we use MoleculeServiceThinExecutorStep for everything
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.ServiceExecutor.CLASSNAME)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, key)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOption(OPT_SERVICE_ENDPOINT, serviceDescriptor.getAccessModes()[0].getExecutionEndpoint())
                    .withOption(OPT_SERVICE_PRESERVE_STRUCTURE, true) // TODO - this will need to be defined at the ServiceDescriptor level
                    .withOption(OPT_SERVICE_PARAMS, collectAllOptions(cell));

            return buildJobDefinition(cellExecutionData, cell, step1);
        }
    }
}
