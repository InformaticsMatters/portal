package portal.notebook.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class NotebookDTO {
    private final List<CellDTO> cellDefinitionList = new ArrayList<>();

    @XmlElement
    public List<CellDTO> getCellDefinitionList() {
        return cellDefinitionList;
    }

}
