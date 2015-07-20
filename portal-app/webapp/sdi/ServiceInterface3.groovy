import portal.service.ServiceDescriptor
import portal.webapp.ServiceDiscoveryInterface
import portal.webapp.ServicePropertyDescriptor

/**
 * @author simetrias
 */
class ServiceInterface3 implements ServiceDiscoveryInterface {

    private ServiceDescriptor serviceDescriptor;

    ServiceInterface3() {
        serviceDescriptor = new ServiceDescriptor();
        serviceDescriptor.setId(3l);
        serviceDescriptor.setName("Identity service");

        serviceDescriptor.setEndpoint("direct:simpleroute");

        List<ServicePropertyDescriptor> servicePropertyDescriptorList = new ArrayList<>();
        serviceDescriptor.setServicePropertyDescriptorList(servicePropertyDescriptorList);
    }

    @Override
    ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }
}
