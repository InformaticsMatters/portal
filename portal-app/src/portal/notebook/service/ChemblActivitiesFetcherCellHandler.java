package portal.notebook.service;

import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import portal.notebook.api.CellDTO;
import portal.notebook.api.CellExecutionClient;
import portal.notebook.api.CellType;
import portal.notebook.api.VariableType;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Given a ChEMBL assay ID fetches all activities for that assay and generated a Dataset or
 * MoleculeObjects containing the structures and activites.
 * Performs this using the REST API provided at the EBI.
 *
 * Created by timbo on 10/11/15.
 */
public class ChemblActivitiesFetcherCellHandler implements CellHandler {

    @Inject
    private CellExecutionClient cellExecutionClient;

    @Override
    public Cell createCell() {
        Cell cell = new Cell();
        cell.setCellType(CellType.CHEMBLACTIVITIESFETCHER);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("results");
        variable.setVariableType(VariableType.DATASET);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(String cellName) {

        CellDTO cell = cellExecutionClient.retrieveCell(cellName);
        String assayID = (String) cell.getPropertyMap().get("assayId");
        String prefix = (String) cell.getPropertyMap().get("prefix");
        // real implmentation class not yet accessible so using the inner class as a mock for now
        ChemblClient client = new ChemblClient();
        // the batchSize of 100 should be thought of as an advanced option - not present in the standard
        // UI but able to be specified using "Advanced" settings. For now we hard code a sensible value.
        Dataset<MoleculeObject> dataset = client.fetchActivitiesForAssay(assayID, 100, prefix);

        try {
            // As it´s a DATASET variable type we write metatada to value and contents as any stream-based variable(like FILE)
            Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
            try (Stream stream = generator.getAsStream()) {
                InputStream dataInputStream = generator.getAsInputStream(stream, true);
                cellExecutionClient.writeStreamContents(cellName, "results", dataInputStream);
            }
            DatasetMetadata metadata = generator.getDatasetMetadata();
            cellExecutionClient.writeTextValue(cellName, "results", JsonHandler.getInstance().objectToJson(metadata));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean handles(CellType cellType) {
        return cellType.equals(CellType.CHEMBLACTIVITIESFETCHER);
    }

    /**
     * This is a mock for the real class
     */
    class ChemblClient {
        Dataset<MoleculeObject> fetchActivitiesForAssay(String assayID, int batchSize, String prefix) {
            List<MoleculeObject> mols = new ArrayList<>();
            mols.add(new MoleculeObject("C", "smiles", Collections.singletonMap(prefix, 1.1)));
            mols.add(new MoleculeObject("CC", "smiles", Collections.singletonMap(prefix, 2.2)));
            mols.add(new MoleculeObject("CCC", "smiles", Collections.singletonMap(prefix, 3.3)));
            return new Dataset<>(MoleculeObject.class, mols);
        }
    }
}
