package portal.workflow;

import java.io.Serializable;

/**
 * @author simetrias
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
