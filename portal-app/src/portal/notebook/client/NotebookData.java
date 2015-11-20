package portal.notebook.client;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class NotebookData implements Serializable {
    private Long id;
    private String name;
    private final List<CellData> cellList = new ArrayList<>();

    @XmlElement
    public List<CellData> getCellList() {
        return cellList;
    }

    public CellData findCell(String cellName) {
        for (CellData cellData : cellList) {
            if (cellData.getName().equals(cellName)) {
                return cellData;
            }
        }
        return null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
