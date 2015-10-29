package portal.workflow;

import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.client.ServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.SessionContext;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
@SessionScoped
public class ServicesSession implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ServicesSession.class.getName());
    private final ServicesClient servicesClient;
    private List<ServiceDescriptor> serviceDescriptors;

    @Inject
    private SessionContext sessionContext;

    public ServicesSession() {
        servicesClient = new ServicesClient();
    }

    public List<ServiceDescriptor> listServiceDescriptors() {
        try {
            serviceDescriptors = servicesClient.getServiceDefinitions(sessionContext.getLoggedInUser());
        } catch (IOException e) {
            serviceDescriptors = new ArrayList<>();
            logger.error(null, e);
        }
        return serviceDescriptors;
    }

    public List<ServiceDescriptor> listServiceDescriptors(String filterPattern) {
        List<ServiceDescriptor> descriptorList = listServiceDescriptors();
        List<ServiceDescriptor> result = new ArrayList<>();
        for (ServiceDescriptor serviceDescriptor : descriptorList) {
            String[] tags = serviceDescriptor.getTags();
            for (String tag : tags) {
                if (tag.startsWith(filterPattern)) {
                    result.add(serviceDescriptor);
                    break;
                }
            }
        }
        return result;
    }

    public ServiceDescriptor findServiceDescriptorById(String id) {
        ServiceDescriptor result = null;
        for (ServiceDescriptor sd : serviceDescriptors) {
            if (id.equals(sd.getId())) {
                result = sd;
                break;
            }
        }
        return result;
    }
}
