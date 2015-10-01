package portal.webapp.notebook;

import java.io.Serializable;
import java.util.List;

public interface Cell extends Serializable {

    CellType getCellType();
    void setName(String name);
    String getName();
    int getPositionLeft();
    void setPositionLeft(int x);
    int getPositionTop();
    void setPositionTop(int y);
    List<Variable> getInputVariableList();
    List<String> getOutputVariableNameList();

}
