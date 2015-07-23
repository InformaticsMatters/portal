package portal.webapp;

import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.ServiceDescriptorSet;
import com.im.lac.services.client.ServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.SessionScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
@SessionScoped
public class ServicesSession implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ServicesSession.class.getName());
    private final ServicesClient servicesClient;
    private Map<ServiceDescriptor, String> serviceDescriptorMap;
    private ArrayList<ServiceDescriptor> serviceDescriptors;

    public ServicesSession() {
        servicesClient = new ServicesClient();
    }

    private List<ServiceDescriptorSet> listServiceDescriptorSets() {
        List<ServiceDescriptorSet> result = null;
        try {
            result = servicesClient.getServiceDefinitions();
        } catch (IOException e) {
            logger.error(null, e);
        }
        return result;
    }

    public List<ServiceDescriptor> listServiceDescriptors() {
        serviceDescriptorMap = new HashMap<>();
        List<ServiceDescriptorSet> sets = listServiceDescriptorSets();
        if (sets != null) {
            for (ServiceDescriptorSet set : sets) {
                for (ServiceDescriptor serviceDescriptor : set.getServiceDescriptors()) {
                    serviceDescriptorMap.put(serviceDescriptor, set.getBaseUrl());
                }
            }
        }

        serviceDescriptors = new ArrayList<>(serviceDescriptorMap.keySet());
        return serviceDescriptors;
    }

    public ServiceDescriptor findServiceDescriptorById(Long id) {
        return serviceDescriptors.get(id.intValue());
    }

    public Long getServiceDescriptorId(ServiceDescriptor serviceDescriptor) {
        return (long) serviceDescriptors.indexOf(serviceDescriptor);
    }

    public String getServiceDescriptorEndpoint(ServiceDescriptor serviceDescriptor) {
        String baseUrl = serviceDescriptorMap.get(serviceDescriptor);
        String relativeUrl = serviceDescriptor.getResourceUrl();
        return baseUrl + relativeUrl;
    }
}
