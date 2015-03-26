package portal.integration;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class Structure implements Serializable {

    private String cdId;
    private String cdFormula;
    private String cdMolweight;
    private String cdStructure;

    public String getCdFormula() {
        return cdFormula;
    }

    @JsonProperty("cd_formula")
    public void setCdFormula(String cdFormula) {
        this.cdFormula = cdFormula;
    }

    public String getCdId() {
        return cdId;
    }

    @JsonProperty("cd_id")
    public void setCdId(String cdId) {
        this.cdId = cdId;
    }

    public String getCdMolweight() {
        return cdMolweight;
    }

    @JsonProperty("cd_molweight")
    public void setCdMolweight(String cdMolweight) {
        this.cdMolweight = cdMolweight;
    }

    public String getCdStructure() {
        return cdStructure;
    }

    @JsonProperty("cd_structure")
    public void setCdStructure(String cdStructure) {
        this.cdStructure = cdStructure;
    }

}
