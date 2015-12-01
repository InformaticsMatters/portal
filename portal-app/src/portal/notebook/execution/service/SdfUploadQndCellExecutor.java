package portal.notebook.execution.service;

import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.notebook.api.CellDTO;
import com.squonk.notebook.api.CellType;
import com.squonk.notebook.client.CallbackClient;
import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
import portal.notebook.service.Variable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class SdfUploadQndCellExecutor implements QndCellExecutor {
    @Inject
    private CallbackClient callbackClient;

    @Override
    public boolean handles(CellType cellType) {
        return "SdfUploader".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {
        CellDTO cell = callbackClient.retrieveCell(cellName);
        String nameFieldName = (String) cell.getPropertyMap().get("NameFieldName");
        System.out.println("NameFieldName: " + nameFieldName);

        try (InputStream is = callbackClient.readStreamValue(cellName, "FileContent")) {
            byte[] bytes = IOUtils.convertStreamToBytes(is, 1000);
            System.out.println("Read " + bytes.length + " bytes");
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
            // As it´s a DATASET variable type we write metatada to value and contents as any stream-based variable(like FILE)
            Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
            try (Stream stream = generator.getAsStream()) {
                InputStream dataInputStream = generator.getAsInputStream(stream, true);
                callbackClient.writeStreamContents(cellName, "Results", dataInputStream);
            }
            DatasetMetadata metadata = generator.getDatasetMetadata();
            callbackClient.writeTextValue(cellName, "Results", JsonHandler.getInstance().objectToJson(metadata));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write dataset", e);
        }

    }

}