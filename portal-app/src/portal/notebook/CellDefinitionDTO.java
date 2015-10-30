package portal.notebook;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class CellDefinitionDTO {
    private String name;
    private CellType cellType;
    private final List<VariableDefinitionDTO> inputVariableDefinitionList = new ArrayList<>();
    private final List<String> outputVariableNameList = new ArrayList<>();
    private final Map<String, Object> propertyMap = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    @XmlElement
    public List<VariableDefinitionDTO> getInputVariableDefinitionList() {
        return inputVariableDefinitionList;
    }

    @XmlElement
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }
}
