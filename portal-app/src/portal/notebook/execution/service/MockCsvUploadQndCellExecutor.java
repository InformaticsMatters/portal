package portal.notebook.execution.service;

import com.im.lac.types.BasicObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.api.OptionDTO;
import org.squonk.notebook.client.CallbackClient;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

public class MockCsvUploadQndCellExecutor implements QndCellExecutor {
    @Inject
    private CallbackClient callbackClient;

    @Override
    public boolean handles(CellType cellType) {
        return "CsvUploader".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {
        CellDTO cellDTO = callbackClient.retrieveCell(cellName);

        for (Map.Entry<String, OptionDTO> e : cellDTO.getOptionMap().entrySet()) {
            System.out.println("CSV OPTS: " + e.getKey() + " -> " + e.getValue().getValue());
        }

        try (InputStream is = callbackClient.readStreamValue(cellName, "fileContent")) {
            byte[] bytes = IOUtils.convertStreamToBytes(is, 1000);
            System.out.println("Read " + bytes.length + " bytes");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


        List<BasicObject> mols = new ArrayList<>();
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));

        Dataset dataset =  new Dataset<>(BasicObject.class, mols);

        // write to Results
        try {
            // As it´s a DATASET variable type we write metatada to value and contents as any stream-based variable(like FILE)
            Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
            try (Stream stream = generator.getAsStream()) {
                InputStream dataInputStream = generator.getAsInputStream(stream, true);
                callbackClient.writeStreamContents(cellName, "results", dataInputStream);
            }
            DatasetMetadata metadata = generator.getDatasetMetadata();
            callbackClient.writeTextValue(cellName, "results", JsonHandler.getInstance().objectToJson(metadata));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write dataset", e);
        }

    }

    Map createMap() {
        Random r = new Random();
        Map map = new HashMap();
        map.put("A", r.nextFloat());
        map.put("B", r.nextInt());
        map.put("C", "Hello " + r.nextInt());
        return map;
    }

}