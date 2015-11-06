package portal.notebook;


import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.inject.Inject;
import javax.script.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScriptCellHandler implements CellHandler {
    @Inject
    private NotebookService notebookService;
    private static final Logger LOGGER = Logger.getLogger(ScriptCellHandler.class.getName());

    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.CODE);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("outcome");
        variable.setVariableType(VariableType.VALUE);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(Long notebookId, String cellName) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Cell cell = notebookContents.findCell(cellName);
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("Groovy");
        Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        /**/
        cell.getInputVariableList().clear();
        for (Cell other : notebookContents.getCellList()) {
            if (other != cell) {
                for (Variable variable : other.getOutputVariableList()) {
                    if (variable.getValue() != null) {
                        String producerName = variable.getProducerCell().getName().replaceAll(" ", "_");
                        bindings.put(producerName + "_" + variable.getName(), variable.getValue());
                    }
                }
            }
        }
        try {
            String code = (String)cell.getPropertyMap().get("code");
            Object result = scriptToVm(engine.eval(code));
            cell.getPropertyMap().put("outcome", result);
            cell.getPropertyMap().put("errorMessage", null);
            cell.getOutputVariableList().get(0).setValue(result);
        } catch (ScriptException se) {
            LOGGER.log(Level.WARNING, se.getMessage());
            cell.getPropertyMap().put("errorMssage", se.getMessage());
        }
        notebookService.storeNotebookContents(notebookId, notebookContents);
    }

    private Object scriptToVm(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror)o;
            Collection<Object> result = new ArrayList<Object>();
            Collection<Object> values = scriptObjectMirror.values();
            for (Object value : values) {
                result.add(scriptToVm(value));
            }
            return result;
        } else {
            return o;
        }
    }

    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.CODE);
    }
}
