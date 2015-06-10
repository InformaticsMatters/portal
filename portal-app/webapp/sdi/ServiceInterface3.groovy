package portal.webapp.sdi

import portal.service.api.ServiceDescriptor
import portal.webapp.ServiceDiscoveryInterface

/**
 * @author simetrias
 */
class ServiceInterface3 implements ServiceDiscoveryInterface {

    private ServiceDescriptor serviceDescriptor;

    @Override
    ServiceDescriptor getServiceDescriptor() {
        if (serviceDescriptor == null) {
            serviceDescriptor = createServiceDescriptor();
        }
        return serviceDescriptor;
    }

    private ServiceDescriptor createServiceDescriptor() {
        serviceDescriptor = new ServiceDescriptor();
        serviceDescriptor.setId(3l);
        serviceDescriptor.setName("Service 3");
        return serviceDescriptor;
    }
}
