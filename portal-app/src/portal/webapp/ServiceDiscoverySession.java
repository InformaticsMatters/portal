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

        serviceDescriptor = new ServiceDescriptor();
        serviceDescriptor.setId(2l);
        serviceDescriptor.setName("Service 2");
        serviceDescriptorMap.put(2l, serviceDescriptor);

        serviceDescriptor = new ServiceDescriptor();
        serviceDescriptor.setId(3l);
        serviceDescriptor.setName("Service 3");
        serviceDescriptorMap.put(3l, serviceDescriptor);
    }

    public ServiceDescriptor findServiceDescriptorById(long id) {
        return serviceDescriptorMap.get(id);
    }

    public List<? extends ServiceDescriptor> listServices(ServiceFilterData serviceFilterData) {
        if (serviceFilterData != null) {
            System.out.println("Searching " + serviceFilterData.getPattern() + " - " + serviceFilterData.getFreeOnly());
        }

        return new ArrayList<>(serviceDescriptorMap.values());
    }
}
