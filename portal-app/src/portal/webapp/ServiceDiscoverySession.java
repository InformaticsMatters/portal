package portal.webapp;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.apache.wicket.cdi.CdiContainer;
import portal.service.api.ServiceDescriptor;

import javax.enterprise.context.SessionScoped;
import java.io.File;
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

    private void loadServices() {
        serviceDescriptorMap = new HashMap<>();
        ServiceDescriptor serviceDescriptor;

        File sdiFolder = new File(PortalWebApplication.get().getServletContext().getRealPath("sdi"));
        File[] files = sdiFolder.listFiles();
        for (File scriptFile : files) {
            if (scriptFile.getName().endsWith("groovy")) {
                serviceDescriptor = instantiateServiceInterface(scriptFile).getServiceDescriptor();
                serviceDescriptorMap.put(serviceDescriptor.getId(), serviceDescriptor);
            }
        }
    }

    public ServiceDescriptor findServiceDescriptorById(long id) {
        return serviceDescriptorMap.get(id);
    }

    public List<ServiceDescriptor> listServices(ServiceFilterData serviceFilterData) {
        if (serviceFilterData != null) {
            System.out.println("Searching " + serviceFilterData.getPattern() + " - " + serviceFilterData.getFreeOnly());
        }

        if (serviceDescriptorMap == null) {
            loadServices();
        }

        return new ArrayList<>(serviceDescriptorMap.values());
    }

    private ServiceDiscoveryInterface instantiateServiceInterface(File scriptFile) {
        try {
            Class interfaceClass = new GroovyClassLoader().parseClass(new GroovyCodeSource(scriptFile));
            ServiceDiscoveryInterface serviceDiscoveryInterface = (ServiceDiscoveryInterface) interfaceClass.newInstance();
            CdiContainer.get().getNonContextualManager().postConstruct(serviceDiscoveryInterface);
            return serviceDiscoveryInterface;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
