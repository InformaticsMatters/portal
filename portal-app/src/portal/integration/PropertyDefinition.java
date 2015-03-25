package portal.integration;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author simetrias
 */
@XmlRootElement
public class PropertyDefinition {

    private Integer id;
    private String originalId;
    private String propertyDescription;
    private Integer estSize;

    public Integer getId() {
        return id;
    }

    @JsonProperty("property_id")
    public void setId(Integer id) {
        this.id = id;
    }

    public String getOriginalId() {
        return originalId;
    }

    @JsonProperty("original_id")
    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public String getPropertyDescription() {
        return propertyDescription;
    }

    @JsonProperty("property_description")
    public void setPropertyDescription(String propertyDescription) {
        this.propertyDescription = propertyDescription;
    }

    public Integer getEstSize() {
        return estSize;
    }

    @JsonProperty("est_size")
    public void setEstSize(Integer estSize) {
        this.estSize = estSize;
    }
}
