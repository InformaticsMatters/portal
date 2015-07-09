import portal.service.ServiceDescriptor
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
        propertyDescriptor.setType(ServicePropertyDescriptor.Type.STRING);
        propertyDescriptor.setLabel("Label 1");
        servicePropertyDescriptorList.add(propertyDescriptor);

        propertyDescriptor = new ServicePropertyDescriptor();
        propertyDescriptor.setType(ServicePropertyDescriptor.Type.STRING);
        propertyDescriptor.setLabel("Label 2");
        servicePropertyDescriptorList.add(propertyDescriptor);

        propertyDescriptor = new ServicePropertyDescriptor();
        propertyDescriptor.setType(ServicePropertyDescriptor.Type.STRUCTURE);
        propertyDescriptor.setLabel("Label 3");
        servicePropertyDescriptorList.add(propertyDescriptor);

        serviceDescriptor.setServicePropertyDescriptorList(servicePropertyDescriptorList);
    }

    @Override
    ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }
}
