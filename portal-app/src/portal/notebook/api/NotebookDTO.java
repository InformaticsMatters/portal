package portal.notebook.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class NotebookDTO {
    private final List<CellDTO> cellList = new ArrayList<>();

    @XmlElement
    public List<CellDTO> getCellList() {
        return cellList;
    }

    public CellDTO findCell(NotebookDTO notebookDefinition, String cellName) {
        for (CellDTO cellDefinition : notebookDefinition.getCellList()) {
            if (cellDefinition.getName().equals(cellName)) {
                return cellDefinition;
            }
        }
        return null;
    }

}
