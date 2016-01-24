package portal.notebook.execution.service;


import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.client.CallbackClient;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScriptQndCellExecutor implements QndCellExecutor {
    private static final Logger LOGGER = Logger.getLogger(ScriptQndCellExecutor.class.getName());
    @Inject
    private CallbackClient callbackClient;

    @Override
    public boolean handles(CellType cellType) {
        return "Script".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {
        CellDTO cellDTO = callbackClient.retrieveCell(cellName);
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("Groovy");
        try {
            //Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
            String code = (String) cellDTO.getOptionMap().get("code").getValue();
            Object result = scriptToVm(engine.eval(code));
            if (result == null) {
                // unassign
            } else {
                callbackClient.writeTextValue(cellDTO.getName(), "outcome", result.toString());
            }
        } catch (ScriptException se) {
            LOGGER.log(Level.WARNING, se.getMessage());
            callbackClient.writeTextValue(cellDTO.getName(), "errorMesage", se.getMessage());
        }

    }

    private Object scriptToVm(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) o;
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

}
