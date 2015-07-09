package portal.service;

import portal.webapp.ServicePropertyDescriptor;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class ServiceDescriptor implements Serializable {

    private Long id;
    private String name;
    private List<ServicePropertyDescriptor> servicePropertyDescriptorList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ServicePropertyDescriptor> getServicePropertyDescriptorList() {
        return servicePropertyDescriptorList;
    }

    public void setServicePropertyDescriptorList(List<ServicePropertyDescriptor> servicePropertyDescriptorList) {
        this.servicePropertyDescriptorList = servicePropertyDescriptorList;
    }
}
