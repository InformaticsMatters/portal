package portal.notebook.execution.service;

import org.squonk.dataset.Dataset;
import org.squonk.util.GroovyScriptExecutor;
import org.squonk.notebook.api.*;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MockGroovyScriptTrustedQndCellExecutor extends AbstractDatasetExecutor {

    private static final Logger LOG = Logger.getLogger(MockGroovyScriptTrustedQndCellExecutor.class.getName());


    @Override
    public boolean handles(CellType cellType) {
        return "TrustedGroovyDatasetScript".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {
        CellDTO cellDTO = callbackClient.retrieveCell(cellName);

        dumpOptions(cellDTO, Level.INFO);

        OptionDTO dto = cellDTO.getOptionMap().get("script");
        String script = null;
        if (dto != null) {
            script = (String)dto.getValue();
        }
        if (script == null || script.length() == 0) {
            throw new IllegalStateException("Script must be defined");
        }
        //LOG.info("Using script = " + script);

        NotebookDTO notebookDefinition = callbackClient.retrieveNotebookDefinition();
        VariableKey inputVariableKey = cellDTO.getBindingMap().get("input").getVariableKey();

        try {
            Dataset input = readDataset(inputVariableKey);

            Map bindings = Collections.singletonMap("input", input);

            ScriptEngine engine = GroovyScriptExecutor.createScriptEngine(this.getClass().getClassLoader());
            Dataset output = GroovyScriptExecutor.executeAndReturnValue(Dataset.class, engine, script, bindings);

            writeDataset(cellDTO, "output", output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write dataset", e);
        } catch (ScriptException e) {
            throw new RuntimeException("Failed to execute script", e);
        }

    }


}