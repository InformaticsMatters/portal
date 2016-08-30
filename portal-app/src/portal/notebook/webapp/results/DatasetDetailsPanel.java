package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by timbo on 27/08/2016.
 */
public class DatasetDetailsPanel extends Panel {

    private final CompoundPropertyModel<DatasetMetadata> datasetMetadataModel = new CompoundPropertyModel<>(new DatasetMetadata(BasicObject.class));
    private final CompoundPropertyModel<List<? extends BasicObject>> resultsModel = new CompoundPropertyModel<>(Collections.emptyList());

    public DatasetDetailsPanel(String id) {
        super(id);
        addContent();
    }

    public <T extends BasicObject> void setDataset(Dataset<T> dataset) throws IOException {
        datasetMetadataModel.setObject(dataset == null ? null : dataset.getMetadata());
        // TODO - need a better way to handle large datsets
        List<T> results = dataset == null ? null : dataset.getStream().limit(100).collect(Collectors.toList());
        resultsModel.setObject(results);
    }

    private void addContent() {

        add(new DatasetResultsPanel("results", datasetMetadataModel, resultsModel));
        add(new DatasetMetadataPanel("metadata", datasetMetadataModel));

    }
}