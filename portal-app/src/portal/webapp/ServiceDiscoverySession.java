package portal.webapp;

import portal.service.api.ServiceDescriptor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author simetrias
 */
public class ServiceDiscoverySession implements Serializable {

    private Map<Long, ServiceDescriptor> serviceDescriptorMap;

    public void loadServiceList() {

    }

}
