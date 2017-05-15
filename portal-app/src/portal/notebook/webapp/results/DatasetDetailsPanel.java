package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import portal.notebook.webapp.AbstractCellDatasetProvider;
import portal.notebook.webapp.OutputVariableCellDatasetProvider;

import java.util.Collections;
import java.util.List;

/**
 * Created by timbo on 27/08/2016.
 */
public class DatasetDetailsPanel extends Panel {

    private final IModel<DatasetMetadata> datasetMetadataModel;
    private final AbstractCellDatasetProvider cellDatasetProvider;
    private Class<? extends BasicObject> datasetType;

    public DatasetDetailsPanel(String id, OutputVariableCellDatasetProvider cellDatasetProvider) {
        this(id, cellDatasetProvider, Collections.emptyList());
    }

    public DatasetDetailsPanel(String id, AbstractCellDatasetProvider cellDatasetProvider, List<Panel> firstPanels) {
        super(id);
        this.cellDatasetProvider = cellDatasetProvider;
        this.datasetMetadataModel = new CompoundPropertyModel<>((DatasetMetadata) null);

        firstPanels.forEach((p) -> add(p));

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