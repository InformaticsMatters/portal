package portal.notebook.execution.service;

import org.squonk.dataset.Dataset;
import org.squonk.notebook.api.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MockTransformValuesQndCellExecutor extends AbstractDatasetExecutor {

    private static Logger LOG = Logger.getLogger(MockTransformValuesQndCellExecutor.class.getName());

    @Override
    public boolean handles(CellType cellType) {
        return "TransformValues".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {
        CellDTO cellDTO = callbackClient.retrieveCell(cellName);

        dumpOptions(cellDTO, Level.INFO);

        OptionDTO dto = cellDTO.getOptionMap().get("transformDefinitions");
        String transformDefinitions = null;
        if (dto != null) {
            transformDefinitions = (String)dto.getValue();
        }
        if (transformDefinitions == null || transformDefinitions.length() == 0) {
            throw new IllegalStateException("Transform definitions must be defined");
        }
        LOG.info("Using transformDefinitions = " + transformDefinitions);

        NotebookDTO notebookDefinition = callbackClient.retrieveNotebookDefinition();
        VariableKey inputVariableKey = cellDTO.getBindingMap().get("input").getVariableKey();

        try {
            Dataset input = readDataset(inputVariableKey);

            // mock does nothing

            writeDataset(cellDTO, "output", input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write dataset", e);
        }

    }


}