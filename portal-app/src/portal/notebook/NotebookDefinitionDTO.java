package portal.notebook;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class NotebookDefinitionDTO {
    private final List<CellDefinitionDTO> cellDefinitionList = new ArrayList<>();
    private final List<VariableDefinitionDTO> variableDefinitionList = new ArrayList<>();

    @XmlElement
    public List<CellDefinitionDTO> getCellDefinitionList() {
        return cellDefinitionList;
    }

    @XmlElement
    public List<VariableDefinitionDTO> getVariableDefinitionList() {
        return variableDefinitionList;
    }
}
