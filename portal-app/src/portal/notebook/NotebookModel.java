package portal.notebook;

import java.io.Serializable;
import java.util.*;

public class NotebookModel implements Serializable {
    private static final Long serialVersionUID = 1l;
    private String name;
    private final List<CellModel> cellModelList = new ArrayList<CellModel>();
    private final List<VariableModel> variableModelList = new ArrayList<>();
    private transient List<NotebookChangeListener> changeListenerList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCell(CellModel cellModel) {
        cellModelList.add(cellModel);
        registerVariablesForProducer(cellModel);
        notifyCellAdded(cellModel);
    }

    private void notifyCellAdded(CellModel cellModel) {
        if (changeListenerList != null) {
            for (NotebookChangeListener listener : changeListenerList) {
                listener.onCellAdded(cellModel);
            }
        }
    }

    private void checkCellName(CellModel cellModel) {
        for (CellModel item : cellModelList) {
            if (item.getName().equalsIgnoreCase(cellModel.getName())) {
                throw new RuntimeException("Cell name already used");
            }
        }
    }

    private String calculateCellName(CellModel cellModel) {
        int typeCount = 0;
        Set<String> nameSet = new HashSet<String>();
        for (CellModel item : cellModelList) {
            if (item.getCellType().equals(cellModel.getCellType())) {
                typeCount++;
            }
            nameSet.add(item.getName());
        }
        int suffix = typeCount + 1;
        String newName = cellModel.getCellType().name() + suffix;
        while (nameSet.contains(newName)) {
            suffix++;
            newName = cellModel.getCellType().name() + suffix;
        }
        return newName;
    }

    public void removeCell(CellModel cellModel) {
        synchronized (variableModelList) {
            VariableModel[] snapshot = variableModelList.toArray(new VariableModel[0]);
            for (VariableModel variableModel : snapshot) {
                if (variableModel.getProducer() == cellModel) {
                    variableModelList.remove(variableModel);
                    variableModel.notifyRemoved();
                }
            }
        }
        synchronized (cellModelList) {
            cellModelList.remove(cellModel);
            notifyCellRemoved(cellModel);
        }
    }

    private void notifyCellRemoved(CellModel cellModel) {
        if (changeListenerList != null) {
            for (NotebookChangeListener listener : changeListenerList) {
                listener.onCellRemoved(cellModel);
            }
        }
    }

    public List<CellModel> getCellModelList() {
        synchronized (cellModelList) {
            return Collections.unmodifiableList(cellModelList);
        }
    }

    public List<VariableModel> getVariableModelList() {
        return Collections.unmodifiableList(variableModelList);
    }

    public VariableModel findVariable(String producerName, String name) {
        for (VariableModel variableModel : variableModelList) {
            if (variableModel.getProducer().getName().equals(producerName) && variableModel.getName().equals(name)) {
                return variableModel;
            }
        }
        return null;
    }

    public Map<String, VariableModel> registerVariablesForProducer(CellModel cellModel) {
        synchronized (variableModelList) {
            Map<String, VariableModel> oldMap = new HashMap<>();
            for (VariableModel variableModel : variableModelList) {
                if (variableModel.getProducer().equals(cellModel)) {
                    oldMap.put(variableModel.getName(), variableModel);
                }
            }
            Map<String, VariableModel> newMap = new HashMap<>();
            for (String name : cellModel.getOutputVariableNameList()) {
                VariableModel variableModel = oldMap.get(name);
                if (variableModel == null) {
                    variableModel = new VariableModel();
                    variableModel.setProducer(cellModel);
                    variableModel.setName(name);
                    variableModelList.add(variableModel);
                } else {
                    oldMap.remove(name);
                }
                newMap.put(name, variableModel);
            }
            for (VariableModel variableModel : oldMap.values()) {
                variableModelList.remove(variableModel);
                variableModel.notifyRemoved();
            }
            return newMap;
        }

    }

    public Map<String, VariableModel> findVariablesForProducer(CellModel cellModel) {
        Map<String, VariableModel> map = new HashMap<>();
        for (VariableModel variableModel : variableModelList) {
            if (variableModel.getProducer().equals(cellModel)) {
                map.put(variableModel.getName(), variableModel);
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

    public void toNotebookContents(NotebookContents notebookContents) {
        Map<String, Cell> cellMap = new HashMap<>();
        for (CellModel cellModel : cellModelList) {
            Cell cell = notebookContents.addCell(cellModel.getCellType());
            cell.setName(cellModel.getName());
            for (String variableName : cellModel.getOutputVariableNameList()) {
                Variable variable = notebookContents.findVariable(cellModel.getName(), variableName);
                VariableModel variableModel = findVariable(cellModel.getName(), variableName);
                variable.setValue(variableModel == null ? null : variableModel.getValue());
            }
            cellMap.put(cellModel.getName(), cell);
            notebookContents.getCellList().add(cell);
        }
        for (CellModel cellModel : cellModelList) {
            Cell cell = cellMap.get(cellModel.getName());
            cellModel.store(notebookContents, cell);
        }

    }

    public void fromNotebookContents(NotebookContents notebookContents) {
        Map<String, CellModel> cellModelMap = new HashMap<>();
        for (Cell cell : notebookContents.getCellList()) {
            CellModel cellModel = createCellModel(cell.getCellType());
            cellModel.setName(cell.getName());
            for (Variable variable : cell.getOutputVariableList()) {
                VariableModel variableModel = new VariableModel();
                variableModel.setName(variable.getName());
                variableModel.setProducer(cellModel);
                variableModel.setValue(variable.getValue());
                variableModel.setVariableType(variable.getVariableType());
                variableModelList.add(variableModel);
            }
            cellModelMap.put(cell.getName(), cellModel);
            cellModelList.add(cellModel);
        }
        for (Cell cell : notebookContents.getCellList()) {
            CellModel cellModel = cellModelMap.get(cell.getName());
            cellModel.load(this, cell);
        }

    }

    public static CellModel createCellModel(CellType cellType) {
        if (CellType.FILE_UPLOAD.equals(cellType)) {
            return new FileUploadCellModel();
        } else if (CellType.CODE.equals(cellType)) {
            return new ScriptCellModel();
        } else if (CellType.PROPERTY_CALCULATE.equals(cellType)) {
            return new PropertyCalculateCellModel();
        } else if (CellType.TABLE_DISPLAY.equals(cellType)) {
            return new TableDisplayCellModel();
        } else {
            return null;
        }
    }


}
