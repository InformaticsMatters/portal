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

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (serviceDescriptor != null && obj != null && obj instanceof ServiceCanvasItemData) {
            ServiceCanvasItemData data = (ServiceCanvasItemData) obj;
            result = data.getServiceDescriptor().getId().equals(getServiceDescriptor().getId());
        }
        return result;
    }
}
