package portal.visualizers;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class VisualizerDescriptor implements Serializable {

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
