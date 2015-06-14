import portal.service.api.ServiceDescriptor
import portal.webapp.ServiceDiscoveryInterface
import portal.webapp.ServicePropertyDescriptor

/**
 * @author simetrias
 */
class ServiceInterface1 implements ServiceDiscoveryInterface {

    private ServiceDescriptor serviceDescriptor;

    ServiceInterface1() {
        serviceDescriptor = new ServiceDescriptor();
        serviceDescriptor.setId(1l);
        serviceDescriptor.setName("Service 1");

        List<ServicePropertyDescriptor> servicePropertyDescriptorList = new ArrayList<>();
        ServicePropertyDescriptor propertyDescriptor;

        propertyDescriptor = new ServicePropertyDescriptor();
        propertyDescriptor.setLabel("Caption 1");
        servicePropertyDescriptorList.add(propertyDescriptor);

        propertyDescriptor = new ServicePropertyDescriptor();
        propertyDescriptor.setLabel("Caption 2");
        servicePropertyDescriptorList.add(propertyDescriptor);

        serviceDescriptor.setServicePropertyDescriptorList(servicePropertyDescriptorList);
    }

    @Override
    ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }
}
