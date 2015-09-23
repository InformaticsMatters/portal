package portal.webapp.notebook;

import java.io.Serializable;
import java.util.List;

public interface Cell extends Serializable {

    CellType getCellType();
    void setName(String name);
    String getName();
    int getX();
    void setX(int x);
    int getY();
    void setY(int y);
    List<Variable> getInputVariableList();
    List<String> getOutputVariableNameList();

}
