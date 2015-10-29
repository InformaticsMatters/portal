package portal.notebook;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VariableDefinitionDTO {
    private String producerName;
    private String name;

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
