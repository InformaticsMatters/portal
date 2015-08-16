package portal.webapp;

import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.ServicePropertyDescriptor;

import java.util.Map;

/**
 * @author simetrias
 */
public class ServiceCanvasItemData extends AbstractCanvasItemData {

    private ServiceDescriptor serviceDescriptor;
    private Map<ServicePropertyDescriptor, String> servicePropertyValueMap;

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public void setServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

    public Map<ServicePropertyDescriptor, String> getServicePropertyValueMap() {
        return servicePropertyValueMap;
    }

    public void setServicePropertyValueMap(Map<ServicePropertyDescriptor, String> servicePropertyValueMap) {
        this.servicePropertyValueMap = servicePropertyValueMap;
    }

    /*
    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (serviceDescriptor != null && obj != null && obj instanceof ServiceCanvasItemData) {
            ServiceCanvasItemData data = (ServiceCanvasItemData) obj;
            result = data.getServiceDescriptor().getId().equals(getServiceDescriptor().getId());
        }
        return result;
    }
    */
}
