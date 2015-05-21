package portal.webapp;

import portal.service.api.ServiceDescriptor;

/**
 * @author simetrias
 */
public class ServiceCanvasItemData extends AbstractCanvasItemData {

    private ServiceDescriptor serviceDescriptor;

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public void setServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }
}
