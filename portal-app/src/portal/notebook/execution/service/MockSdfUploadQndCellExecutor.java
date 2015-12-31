package portal.notebook.execution.service;

import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.util.IOUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MockSdfUploadQndCellExecutor extends AbstractDatasetExecutor {

    private static final Logger LOG = Logger.getLogger(MockSdfUploadQndCellExecutor.class.getName());

    @Inject
    private CallbackClient callbackClient;

    @Override
    public boolean handles(CellType cellType) {
        return "SdfUploader".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {
        CellDTO cellDTO = callbackClient.retrieveCell(cellName);

        dumpOptions(cellDTO, Level.INFO);

        try (InputStream is = callbackClient.readStreamValue(cellName, "fileContent")) {
            byte[] bytes = IOUtils.convertStreamToBytes(is, 1000);
            LOG.info("Read " + bytes.length + " bytes");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        List<MoleculeObject> mols = new ArrayList<>();
        mols.add(new MoleculeObject("C", "smiles", Collections.singletonMap("X", 1.1)));
        mols.add(new MoleculeObject("CC", "smiles", Collections.singletonMap("X", 2.2)));
        mols.add(new MoleculeObject("CCC", "smiles", Collections.singletonMap("X", 3.3)));
        Dataset dataset =  new Dataset<>(MoleculeObject.class, mols);

        // write to Results
        try {
            writeDataset(cellDTO, "results", dataset);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write dataset", e);
        }

    }

}