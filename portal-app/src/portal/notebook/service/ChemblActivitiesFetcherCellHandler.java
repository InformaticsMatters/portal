package portal.notebook.service;

import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import portal.notebook.api.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        //cell.setCellType(CellType.???);
        Variable variable = new Variable();
        variable.setProducerCell(cell);
        variable.setName("Results");
        //variable.setVariableType(VariableType.???);
        cell.getOutputVariableList().add(variable);
        return cell;
    }

    @Override
    public void execute(Long notebookId, String cellName) {
        // not sure what's needed here
        cellExecutionClient.setUriBase("http://localhost:8080/ws/cell");
        // better to request cell using notebook and cell ID's?
        // - that way cell executor does not gain access to whole notebook?
        NotebookDTO notebookDTO = cellExecutionClient.retrieveNotebookDefinition(notebookId);
        CellDTO cell = notebookDTO.findCell(notebookDTO, cellName);
        String assayID = (String) cell.getPropertyMap().get("AssayID");
        String prefix = (String) cell.getPropertyMap().get("Prefix");
        // real implmentation class not yet accessible so using the inner class as a mock for now
        ChemblClient client = new ChemblClient();
        // the batchSize of 100 should be thought of as an advanced option - not present in the standard
        // UI but able to be specified using "Advanced" settings. For now we hard code a sensible value.
        Dataset<MoleculeObject> dataset = client.fetchActivitiesForAssay(assayID, 100, prefix);

        // this code needs an updated version of the libraries
        // the dataset need to be written in 2 parts:
        // 1. the Stream<MoleculeObject> as a (poentially very large) byte stream
        // 2. the metedata as small bit of JSON
//        Dataset.DatasetMetadataGenerator generator = ds.createDatasetMetadataGenerator();
//        try (Stream s = generator.getAsStream()) {
//            InputStream is = generator.getAsInputStream(s, true);
//            // write to variable as stream of bytes
//            //loader.writeToBytes(name + "#DATA", is);
//        } // stream now closed
//        // now write the metadata as JSON
//        DatasetMetadata md = (DatasetMetadata)generator.getDatasetMetadata();
//        //loader.writeToJson(name + "#META", md);

    }

    @Override
    public boolean handles(CellType cellType) {
        return false;
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
