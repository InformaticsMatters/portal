package portal.webapp.notebook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notebook implements Serializable {
    private String name;
    private final Map<String, Variable> variableMap = new HashMap<String, Variable>();
    private final List<Cell> cellList = new ArrayList<Cell>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Variable> getVariableMap() {
        return variableMap;
    }

    public List<Cell> getCellList() {
        return cellList;
    }
}
