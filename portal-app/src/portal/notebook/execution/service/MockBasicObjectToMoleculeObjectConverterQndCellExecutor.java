package portal.notebook.execution.service;

import com.im.lac.types.TypesUtils;
import org.squonk.dataset.Dataset;
import org.squonk.notebook.api.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MockBasicObjectToMoleculeObjectConverterQndCellExecutor extends AbstractDatasetExecutor {

    private static Logger LOG = Logger.getLogger(MockBasicObjectToMoleculeObjectConverterQndCellExecutor.class.getName());

    @Override
    public boolean handles(CellType cellType) {
        return "BasicObjectToMoleculeObject".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {
        CellDTO cellDTO = callbackClient.retrieveCell(cellName);

        dumpOptions(cellDTO, Level.INFO);

        OptionDTO dto = cellDTO.getOptionMap().get("structureFieldName");
        String structureFieldName = null;
        if (dto != null) {
            structureFieldName = (String)dto.getValue();
        }
        if (structureFieldName == null || structureFieldName.length() == 0) {
            throw new IllegalStateException("Structure field name must be defined");
        }
        LOG.info("Using structureFieldName = " + structureFieldName);

        NotebookDTO notebookDefinition = callbackClient.retrieveNotebookDefinition();
        VariableKey inputVariableKey = cellDTO.getBindingMap().get("input").getVariableKey();

        try {
            Dataset input = readDataset(inputVariableKey);

            // convert the dataset
            Dataset output = TypesUtils.convertBasicObjectDatasetToMoleculeObjectDataset(input, structureFieldName, "smiles", true);
            //Dataset output = input;

            writeDataset(cellDTO, "output", output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write dataset", e);
        }

    }


}