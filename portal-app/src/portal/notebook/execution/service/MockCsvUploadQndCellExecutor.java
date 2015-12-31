package portal.notebook.execution.service;

import com.im.lac.types.BasicObject;
import org.squonk.dataset.Dataset;
import org.squonk.util.IOUtils;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.client.CallbackClient;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MockCsvUploadQndCellExecutor extends AbstractDatasetExecutor {

    private static Logger LOG = Logger.getLogger(AbstractDatasetExecutor.class.getName());

    @Inject
    private CallbackClient callbackClient;

    @Override
    public boolean handles(CellType cellType) {
        return "CsvUploader".equals(cellType.getName());
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
            writeDataset(cellDTO, "results", dataset);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write dataset", e);
        }

    }

    Map createMap() {
        Random r = new Random();
        Map map = new HashMap();
        int i = r.nextInt();
        map.put("A", r.nextFloat());
        map.put("B", i);
        map.put("C", "Hello " + i);
        StringBuilder b = new StringBuilder();

        for (int j=-1; j<Math.abs(i%5); j++) {
            b.append("C");
        }
        map.put("S", b.toString());
        return map;
    }

}