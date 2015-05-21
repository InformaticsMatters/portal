package portal.service.api;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class ServiceDescriptor implements Serializable {

    private Long id;
    private String name;

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
}
