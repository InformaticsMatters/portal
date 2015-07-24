package portal.webapp;

import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.client.ServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.SessionScoped;
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

    public ServicesSession() {
        servicesClient = new ServicesClient();
    }

    public List<ServiceDescriptor> listServiceDescriptors() {
        try {
            serviceDescriptors = servicesClient.getServiceDefinitions();
        } catch (IOException e) {
            serviceDescriptors = new ArrayList<>();
            logger.error(null, e);
        }
        return serviceDescriptors;
    }

    public ServiceDescriptor findServiceDescriptorById(Long id) {
        return serviceDescriptors.get(id.intValue());
    }

}
