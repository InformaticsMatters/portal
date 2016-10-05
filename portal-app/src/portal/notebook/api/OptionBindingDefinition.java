package portal.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class OptionBindingDefinition implements Serializable {

    private final static long serialVersionUID = 1L;
    private String key;
    private String name;
    private String displayName;

    public OptionBindingDefinition() {
    }

    public OptionBindingDefinition(String key, String name, String displayName) {
        this.key = key;
        this.name = name;
        this.displayName = displayName;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
