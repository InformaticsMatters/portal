package portal.notebook;

import com.squonk.notebook.api.CellType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface CellModel extends Serializable {

    CellType getCellType();
    void setName(String name);
    String getName();
    int getPositionLeft();
    void setPositionLeft(int x);
    int getPositionTop();
    void setPositionTop(int y);
    List<String> getOutputVariableNameList();
    void store(NotebookContents notebookContents, Cell cell);
    void load(NotebookModel notebookContents, Cell cell);

    List<BindingModel> getBindingModelList();

    Map<String, Object> getOptionMap();

}
