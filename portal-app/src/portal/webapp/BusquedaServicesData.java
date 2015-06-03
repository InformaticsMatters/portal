package portal.webapp;

import java.io.Serializable;

/**
 * Created by mariapaz on 6/3/15.
 */
public class BusquedaServicesData implements Serializable {

    private String name;
    private Boolean freeOnly;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFreeOnly() {
        return freeOnly;
    }

    public void setFreeOnly(Boolean freeOnly) {
        this.freeOnly = freeOnly;
    }
}
