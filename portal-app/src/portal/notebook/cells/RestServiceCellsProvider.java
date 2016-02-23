package portal.notebook.cells;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.core.CommonConstants;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.client.ServicesClient;
import org.squonk.options.OptionDescriptor;
import portal.SessionContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
@ApplicationScoped
@Default
public class RestServiceCellsProvider implements ServiceCellsProvider {

    private static final Logger logger = LoggerFactory.getLogger(RestServiceCellsProvider.class);
    @Inject
    private SessionContext sessionContext;

    @Override
    public List<ServiceCellDefinition> listServiceCellDefinition() {
        ArrayList<ServiceCellDefinition> result = new ArrayList<>();
        for (ServiceDescriptor serviceDescriptor : listServiceDescriptors()) {
            result.add(buildCellDefinitionForServiceDescriptor(serviceDescriptor));
        }
        return result;
    }

    private List<ServiceDescriptor> listServiceDescriptors() {
        ServicesClient servicesClient = new ServicesClient(CommonConstants.HOST_CORE_SERVICES_SERVICES);
        List<ServiceDescriptor> serviceDescriptors;
        try {
            serviceDescriptors = servicesClient.getServiceDescriptors(sessionContext.getLoggedInUserDetails().getUserid());
        } catch (Throwable e) {
            serviceDescriptors = new ArrayList<>();
            logger.error(null, e);
        }
        return serviceDescriptors;
    }

    private ServiceCellDefinition buildCellDefinitionForServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        ServiceCellDefinition result = new ServiceCellDefinition(serviceDescriptor);
        OptionDescriptor[] parameters = serviceDescriptor.getAccessModes()[0].getParameters();
        if (parameters != null) {
            logger.info(parameters.length + " parameters found for service " + serviceDescriptor.getName());
            for (OptionDescriptor parameter : parameters) {
                logger.info("property type: " + parameter.getTypeDescriptor().getType());
                result.getOptionDefinitionList().add(parameter);
            }
        }

        return result;
    }
}
