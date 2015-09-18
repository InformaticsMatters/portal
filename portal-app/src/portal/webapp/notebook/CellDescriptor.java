package portal.webapp.notebook;

import java.io.Serializable;
import java.util.List;

public interface CellDescriptor extends Serializable {

    CellType getCellType();
    void setName(String name);
    String getName();
    int getX();
    void setX(int x);
    int getY();
    void setY(int y);
    List<String> getInputVariableNameList();
    List<String> getOutputVariableNameList();


}
