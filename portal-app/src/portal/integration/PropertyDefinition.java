package portal.integration;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author simetrias
 */
@XmlRootElement
public class PropertyDefinition {

    private Integer propertyId;
    private String propertyDescription;
    private Integer estSize;

    public Integer getPropertyId() {
        return propertyId;
    }

    @JsonProperty("property_id")
    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
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
