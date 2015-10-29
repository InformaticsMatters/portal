package portal.workflow;

import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.ServicePropertyDescriptor;

import java.util.Map;

/**
 * @author simetrias
 */
public class ServiceCanvasItemData extends AbstractCanvasItemData {

    private ServiceDescriptor serviceDescriptor;
    private Map<ServicePropertyDescriptor, String> servicePropertyValueMap;
    private Boolean createOutputFile;
    private String outputFileName;

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

    public Boolean getCreateOutputFile() {
        return createOutputFile;
    }

    public void setCreateOutputFile(Boolean createOutputFile) {
        this.createOutputFile = createOutputFile;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
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
