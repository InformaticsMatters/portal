package portal.notebook.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.core.ServiceConfig;
import org.squonk.core.client.ServicesRestClient;
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
        for (ServiceConfig serviceConfig : listServiceConfigss()) {
            result.add(buildCellDefinitionForServiceDescriptor(serviceConfig));
            logger.info("Adding service cell " + serviceConfig.getId() + " -> " + serviceConfig.getName());
        }
        return result;
    }

    private List<ServiceConfig> listServiceConfigss() {
        ServicesRestClient servicesClient = new ServicesRestClient();
        List<ServiceConfig> serviceConfigs;
        try {
            serviceConfigs = servicesClient.getServiceConfigs(sessionContext.getLoggedInUserDetails().getUserid());
        } catch (Throwable e) {
            serviceConfigs = new ArrayList<>();
            logger.error(null, e);
        }
        return serviceConfigs;
    }

    private ServiceCellDefinition buildCellDefinitionForServiceDescriptor(ServiceConfig serviceConfig) {
        ServiceCellDefinition result = new ServiceCellDefinition(serviceConfig);
        return result;
    }

}
