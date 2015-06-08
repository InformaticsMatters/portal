package portal.webapp;

import java.io.Serializable;

/**
 * Created by mariapaz on 6/3/15.
 */
public class SearchServiceData implements Serializable {

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
