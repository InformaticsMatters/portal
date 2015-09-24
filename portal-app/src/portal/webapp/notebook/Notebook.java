package portal.webapp.notebook;

import java.io.Serializable;
import java.util.*;

public class Notebook implements Serializable {
    private String name;
    private final List<Cell> cellList = new ArrayList<Cell>();
    private final List<Variable> variableList = new ArrayList<>();
    private transient List<NotebookChangeListener> changeListenerList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCell(Cell cell) {
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
        notifyCellAdded(cell);
    }

    private void notifyCellAdded(Cell cell) {
        if (changeListenerList != null) {
            for (NotebookChangeListener listener : changeListenerList) {
                listener.onCellAdded(cell);
            }
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

    public void removeCell(Cell cell) {
        synchronized (variableList) {
            Variable[] snapshot = variableList.toArray(new Variable[0]);
            for (Variable variable : snapshot) {
                if (variable.getProducer() == cell) {
                    variableList.remove(variable);
                    variable.notifyRemoved();
                }
            }
        }
        synchronized (cellList) {
            cellList.remove(cell);
            notifyCellRemoved(cell);
        }
    }

    private void notifyCellRemoved(Cell cell) {
        if (changeListenerList != null) {
            for (NotebookChangeListener listener : changeListenerList) {
                listener.onCellRemoved(cell);
            }
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

    public Map<String, Variable> registerVariablesForProducer(Cell cell) {
        synchronized (variableList) {
            Map<String, Variable> oldMap = new HashMap<>();
            for (Variable variable : variableList) {
                if (variable.getProducer().equals(cell)) {
                    oldMap.put(variable.getName(), variable);
                }
            }
            Map<String, Variable> newMap = new HashMap<>();
            for (String name : cell.getOutputVariableNameList()) {
                Variable variable = oldMap.get(name);
                if (variable == null) {
                    variable = new Variable();
                    variable.setProducer(cell);
                    variable.setName(name);
                    variableList.add(variable);
                } else {
                    oldMap.remove(name);
                }
                newMap.put(name, variable);
            }
            for (Variable variable : oldMap.values()) {
                variableList.remove(variable);
                variable.notifyRemoved();
            }
            return newMap;
        }

    }

    public Map<String, Variable> findVariablesForProducer(Cell cell) {
        Map<String, Variable> map = new HashMap<>();
        for (Variable variable : variableList) {
            if (variable.getProducer().equals(cell)) {
                map.put(variable.getName(), variable);
            }
        }
        return map;
    }

    public synchronized void addNotebookChangeListener(NotebookChangeListener notebookChangeListener) {
        if (changeListenerList == null) {
            changeListenerList = new ArrayList<>();
        }
        changeListenerList.add(notebookChangeListener);
    }

    public void removeNotebookChangeListener(NotebookChangeListener notebookChangeListener) {
        if (changeListenerList != null) {
            changeListenerList.remove(notebookChangeListener);
        }
    }


}
