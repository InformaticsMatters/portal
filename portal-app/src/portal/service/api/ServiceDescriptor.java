package portal.service.api;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class ServiceDescriptor implements Serializable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
