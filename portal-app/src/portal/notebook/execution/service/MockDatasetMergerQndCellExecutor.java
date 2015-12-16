package portal.notebook.execution.service;

import com.im.lac.types.BasicObject;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import org.squonk.notebook.api.BindingDTO;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.api.VariableKey;
import org.squonk.notebook.client.CallbackClient;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

public class MockDatasetMergerQndCellExecutor implements QndCellExecutor {
    @Inject
    private CallbackClient callbackClient;

    @Override
    public boolean handles(CellType cellType) {
        return "DatasetMerger".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {
        CellDTO cellDTO = callbackClient.retrieveCell(cellName);

        for (BindingDTO bindingDTO : cellDTO.getBindingMap().values()) {
            VariableKey variableKey = bindingDTO.getVariableKey();
            String varableFqn = variableKey == null ? null : (variableKey.getProducerName() + "." + variableKey.getName());
            System.out.println(bindingDTO.getName() + ": " + varableFqn);
        }

        List<BasicObject> mols = new ArrayList<>();
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));

        Dataset dataset = new Dataset<>(BasicObject.class, mols);

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