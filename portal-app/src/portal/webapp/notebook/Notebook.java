package portal.webapp.notebook;

import java.io.Serializable;
import java.util.*;

public class Notebook implements Serializable {
    private String name;
    private final List<Cell> cellList = new ArrayList<Cell>();
    private final List<Variable> variableList = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void registerCell(Cell cell) {
        synchronized (cellList) {
            String name = cell.getName();
            if (name == null) {
                name = calculateCellName(cell);
                cell.setName(name);
            } else {
                checkCellName(cell);
            }
            cellList.add(cell);
        }
    }

    private void checkCellName(Cell cell) {
        for (Cell item : cellList) {
            if (item.getName().equalsIgnoreCase(cell.getName())) {
                throw new RuntimeException("Cell name already used");
            }
        }
    }

    private String calculateCellName(Cell cell) {
        int typeCount = 0;
        Set<String> nameSet = new HashSet<String>();
        for (Cell item : cellList) {
            if (item.getCellType().equals(cell.getCellType())) {
                typeCount++;
            }
            nameSet.add(item.getName());
        }
        int suffix = typeCount + 1;
        String newName = cell.getCellType().name() + suffix;
        while (nameSet.contains(newName)) {
            suffix++;
            newName = cell.getCellType().name() + suffix;
        }
        return newName;
    }

    public void unregisterCell(Cell cell) {
        unregisterVariablesForProducer(cell);
        synchronized (cellList) {
            cellList.remove(cell);
        }
    }

    public List<Cell> getCellList() {
        synchronized (cellList) {
            return Collections.unmodifiableList(cellList);
        }
    }

    public List<Variable> getVariableList() {
        return Collections.unmodifiableList(variableList);
    }

    public Variable findVariable(Cell producer, String name) {
        for (Variable variable : variableList) {
            if (variable.getProducer() == producer && variable.getName().equals(name)) {
                return variable;
            }
        }
        return null;
    }

    public void registerVariables(List<Variable> variableList) {
        this.variableList.addAll(variableList);
    }

    public void unregisterVariablesForProducer(Cell cell) {
        synchronized (variableList) {
            Variable[] variables = variableList.toArray(new Variable[0]);
            for (Variable variable : variables) {
                if (variable.getProducer() == cell) {
                    variableList.remove(variable);
                }
            }
        }
    }


}
