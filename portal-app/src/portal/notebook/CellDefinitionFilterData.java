package portal.notebook;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class CellDefinitionFilterData implements Serializable {

    private String pattern;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}