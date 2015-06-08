package portal.webapp;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class ServicesFilterData implements Serializable {

    private String pattern;
    private Boolean freeOnly;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Boolean getFreeOnly() {
        return freeOnly;
    }

    public void setFreeOnly(Boolean freeOnly) {
        this.freeOnly = freeOnly;
    }
}
