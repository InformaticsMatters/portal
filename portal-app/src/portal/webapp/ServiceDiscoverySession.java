package portal.webapp;

import portal.service.api.ServiceDescriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class ServiceDiscoverySession implements Serializable {

    private Map<Long, ServiceDescriptor> serviceDescriptorMap;

    public void loadServiceList() {
        serviceDescriptorMap = new HashMap<Long, ServiceDescriptor>();
        ServiceDescriptor serviceDescriptor;
        serviceDescriptor = new ServiceDescriptor();
        serviceDescriptor.setName("Service 1");
        serviceDescriptorMap.put(1l, serviceDescriptor);
    }

    public List<ServiceDescriptor> getServiceDescriptorList() {
        return new ArrayList<>(serviceDescriptorMap.values());
    }

}
