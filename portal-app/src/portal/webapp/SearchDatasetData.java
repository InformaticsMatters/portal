package portal.webapp;

import java.io.Serializable;

/**
 * Created by mariapaz on 6/4/15.
 */
public class SearchDatasetData implements Serializable {

    private String pattern;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}