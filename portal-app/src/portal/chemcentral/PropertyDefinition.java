package portal.chemcentral;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class PropertyDefinition implements Serializable {

    private Long id;
    private String originalId;
    private String propertyDescription;
    private Integer estSize;

    public Long getId() {
        return id;
    }

    @JsonProperty("property_id")
    public void setId(Long id) {
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
