package portal.webapp;

import portal.service.api.ServiceDescriptor;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
@SessionScoped
public class ServiceDiscoverySession implements Serializable {

    private Map<Long, ServiceDescriptor> serviceDescriptorMap;

    public void loadServices() {
        serviceDescriptorMap = new HashMap<>();
        ServiceDescriptor serviceDescriptor;
        serviceDescriptor = new ServiceDescriptor();
        serviceDescriptor.setId(1l);
        serviceDescriptor.setName("Service 1");
        serviceDescriptorMap.put(1l, serviceDescriptor);
    }

    public List<ServiceDescriptor> getServiceDescriptorList() {
        return new ArrayList<>(serviceDescriptorMap.values());
    }

    public ServiceDescriptor findServiceDescriptorById(long id) {
        return serviceDescriptorMap.get(id);
    }
}
