package portal.notebook.execution.service;

import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.OptionDTO;
import org.squonk.notebook.api.VariableKey;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 28/12/15.
 */
public abstract class AbstractDatasetExecutor implements QndCellExecutor {

    private static Logger LOG = Logger.getLogger(AbstractDatasetExecutor.class.getName());

    @Inject
    protected CallbackClient callbackClient;

    protected void dumpOptions(CellDTO cellDTO, Level level) {
        for (Map.Entry<String, OptionDTO> e : cellDTO.getOptionMap().entrySet()) {
            LOG.log(level, "OPT: " + e.getKey() + " -> " + e.getValue().getValue());
        }
    }

    protected void writeDataset(CellDTO cellDTO, String name, Dataset dataset) throws IOException {
        Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
        try (Stream stream = generator.getAsStream()) {
            InputStream dataInputStream = generator.getAsInputStream(stream, true);
            callbackClient.writeStreamContents(cellDTO.getName(), name, dataInputStream);
        }
        DatasetMetadata metadata = generator.getDatasetMetadata();
        callbackClient.writeTextValue(cellDTO.getName(), name, JsonHandler.getInstance().objectToJson(metadata));
        LOG.info("Wrote dataset. Metadata: " + metadata);
    }

    protected Dataset readDataset(VariableKey variableKey) throws IOException {
        String json = callbackClient.readTextValue(variableKey.getProducerName(), variableKey.getName());
        LOG.fine("Read meatadata json: " + json);
        DatasetMetadata meta = JsonHandler.getInstance().objectFromJson(json, DatasetMetadata.class);
        if (meta == null) {
            return null;
        }
        LOG.fine("Read Items in metadata: " + meta);
        InputStream is = callbackClient.readStreamValue(variableKey.getProducerName(), variableKey.getName());
        if (is == null) {
            return null;
        }
        return JsonHandler.getInstance().unmarshalDataset(meta, IOUtils.getGunzippedInputStream(is));
    }
}
