package portal.notebook.api;


import java.io.Serializable;
import java.util.List;

public interface CellClient extends Serializable {

    List<CellType> listCellType();

    public void executeCell(Long notebookId, String cellName);

    CellType retrieveCellType(String name);
}
