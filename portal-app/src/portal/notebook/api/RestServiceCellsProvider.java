package portal.notebook.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        ServicesClient servicesClient = new ServicesClient();
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
        OptionDescriptor[] options = serviceDescriptor.getOptions();
        if (options != null) {
            logger.info(options.length + " parameters found for service " + serviceDescriptor.getName());
            for (OptionDescriptor option : options) {
                logger.info("property type: " + option.getTypeDescriptor().getType());
                result.getOptionDefinitionList().add(option);
            }
        }
        return result;
    }
}
