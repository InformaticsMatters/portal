package portal.notebook.execution.service;

import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.client.CallbackClient;

import javax.inject.Inject;
import java.util.*;
import java.util.logging.Level;

/**
 * Given a ChEMBL assay ID fetches all activities for that assay and generated a Dataset or
 * MoleculeObjects containing the structures and activities.
 * Performs this using the REST API provided at the EBI.
 *
 * Created by timbo on 10/11/15.
 */
public class ChemblActivitiesFetcherQndCellExecutor extends AbstractDatasetExecutor {
    @Inject
    private CallbackClient callbackClient;


    @Override
    public boolean handles(CellType cellType) {
        return "ChemblActivitiesFetcher".equals(cellType.getName());
    }

    @Override
    public void execute(String cellName) {
        CellDTO cellDTO = callbackClient.retrieveCell(cellName);

        dumpOptions(cellDTO, Level.INFO);

        String assayID = (String) cellDTO.getOptionMap().get("assayId").getValue();
        String prefix = (String) cellDTO.getOptionMap().get("prefix").getValue();
        // real implmentation class not yet accessible so using the inner class as a mock for now
        ChemblClient client = new ChemblClient();
        // the batchSize of 100 should be thought of as an advanced option - not present in the standard
        // UI but able to be specified using "Advanced" settings. For now we hard code a sensible value.
        Dataset<MoleculeObject> dataset = client.fetchActivitiesForAssay(assayID, 100, prefix);

        try {
            writeDataset(cellDTO, "results", dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * This is a mock for the real class
     */
    class ChemblClient {
        Dataset<MoleculeObject> fetchActivitiesForAssay(String assayID, int batchSize, String prefix) {
            List<MoleculeObject> mols = new ArrayList<>();
            Map values = new HashMap();
            values.put("ID", 1);
            values.put(prefix, 1.1);
            mols.add(new MoleculeObject("C", "smiles", values));
            values.put("ID", 2);
            values.put(prefix, 2.2);
            mols.add(new MoleculeObject("CC", "smiles", values));
            values.put("ID", 3);
            values.put(prefix, 3.3);
            mols.add(new MoleculeObject("CCC", "smiles", values));
            return new Dataset<>(MoleculeObject.class, mols);
        }
    }
}
