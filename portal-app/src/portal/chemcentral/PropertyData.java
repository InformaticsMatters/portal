package portal.chemcentral;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

/**
 * @author simetrias
 */
public class PropertyData implements Serializable {

    private Long propertyId;
    private Long structureId;
    private Map<String, String> data;

    public Long getPropertyId() {
        return propertyId;
    }

    @JsonProperty("property_id")
    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public Long getStructureId() {
        return structureId;
    }

    @JsonProperty("structure_id")
    public void setStructureId(Long structureId) {
        this.structureId = structureId;
    }

    public Map<String, String> getData() {
        return data;
    }

    @JsonProperty("property_data")
    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
