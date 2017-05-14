package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import portal.notebook.webapp.DefaultCellDatasetProvider;

/**
 * Created by timbo on 27/08/2016.
 */
public class DatasetDetailsPanel extends Panel {

    private final IModel<DatasetMetadata> datasetMetadataModel;
    private final DefaultCellDatasetProvider cellDatasetProvider;
    private Class<? extends BasicObject> datasetType;

    public DatasetDetailsPanel(String id, DefaultCellDatasetProvider cellDatasetProvider) {
        super(id);
        this.cellDatasetProvider = cellDatasetProvider;
        this.datasetMetadataModel = new CompoundPropertyModel<>((DatasetMetadata) null);

        addDummyContent();
    }


    public <T extends BasicObject> boolean prepare(DatasetMetadata<T> meta) throws Exception {
        if (meta == null) {
            addDummyContent();
            datasetType = null;
            return false;
        }
        datasetMetadataModel.setObject(meta);
        if (datasetType == null || datasetType != meta.getType()) {
            // first time through or when the dataset type has changed
            datasetType = meta.getType();
            addRealContent();
        } else {
            // called when the viewer is re-opened so we need to reload the data in case it has changed
            DatasetResultsPanel rp = (DatasetResultsPanel)get("results");

            rp.reload();
        }
        datasetMetadataModel.setObject(meta);
        return true;
    }

    private void addRealContent() {

        addOrReplace(new DatasetResultsPanel("results", datasetMetadataModel, cellDatasetProvider));
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