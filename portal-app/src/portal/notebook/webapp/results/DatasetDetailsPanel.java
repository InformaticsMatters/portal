package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by timbo on 27/08/2016.
 */
public class DatasetDetailsPanel extends Panel {

    private final IModel<DatasetMetadata> datasetMetadataModel;
    private final DatasetResultsHandler.CellDatasetProvider cellDatasetProvider;
    private final IModel<List<? extends BasicObject>> resultsModel;
    private final IModel<Map<String,Object>> settingsModel;
    private Class<? extends BasicObject> datasetType;

    public DatasetDetailsPanel(String id, DatasetResultsHandler.CellDatasetProvider cellDatasetProvider) {
        super(id);
        this.cellDatasetProvider = cellDatasetProvider;
        this.datasetMetadataModel = new CompoundPropertyModel<>((DatasetMetadata)null);
        this.resultsModel = new CompoundPropertyModel<>(Collections.singletonList(null));
        this.settingsModel = new Model(new LinkedHashMap<String,Object>());

        addDummyContent();
    }


    public <T extends BasicObject> boolean prepare(Dataset<T> dataset) throws Exception {
        if (dataset == null) {
            addDummyContent();
            datasetType = null;
            return false;
        }
        if (datasetType == null || datasetType != dataset.getType()) {
            datasetType = dataset.getType();
            addRealContent();
        }
        datasetMetadataModel.setObject(dataset == null ? null : dataset.getMetadata());
        settingsModel.setObject(cellDatasetProvider.getCellInstance().getSettings());
        return true;
    }

    private void addRealContent() {

        addOrReplace(new DatasetResultsPanel("results", datasetMetadataModel, resultsModel, settingsModel, cellDatasetProvider));
        addOrReplace(new DatasetMetadataPanel("metadata", datasetMetadataModel));
        if (datasetType == MoleculeObject.class) {
            addOrReplace(new MoleculeObjectExportPanel("export", cellDatasetProvider));
        } else {
            addOrReplace(new WebMarkupContainer("export"));
        }
    }

    private void addDummyContent() {
        addOrReplace(new WebMarkupContainer("results"));
        addOrReplace(new WebMarkupContainer("metadata"));
        addOrReplace(new WebMarkupContainer("export"));
    }

}