package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.squonk.dataset.Dataset;
import org.squonk.util.CommonMimeTypes;
import portal.notebook.webapp.AbstractCellDatasetProvider;

import java.io.InputStream;
import java.util.logging.Logger;

/** Export panel for MoleculeObjects.
 * This adds SDF export to the standard formats provided by BasicObject
 *
 * Created by timbo on 02/09/16.
 */
public class MoleculeObjectExportPanel extends BasicObjectExportPanel {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectExportPanel.class.getName());


    public MoleculeObjectExportPanel(String id, AbstractCellDatasetProvider cellDatasetProvider) {

        // superclass handles results as json, csv and tsv
        super(id, cellDatasetProvider);

        String cellName = cellDatasetProvider.getCellInstance().getName();

        // results as SDF
        ResourceLink sdfLink = new ResourceLink("sdfLink", new SDFJsonExportResource(cellName + ".sdf", false));
        sdfLink.add(new Label("sdfFilename", cellName + ".sdf"));
        add(sdfLink);

        ResourceLink sdfgzLink = new ResourceLink("sdfgzLink", new SDFJsonExportResource(cellName + ".sdf.gz", true));
        sdfgzLink.add(new Label("sdfgzFilename", cellName + ".sdf.gz"));
        add(sdfgzLink);
    }

    class SDFJsonExportResource extends AbstractDatasetJsonExportResource {

        SDFJsonExportResource(String filename, boolean gzip) {
            super(filename, CommonMimeTypes.MIME_TYPE_MDL_SDF, gzip);
        }

        @Override
        @SuppressWarnings("unchecked")
        InputStream fetchResults(boolean gzip) throws Exception {
            Dataset data = cellDatasetProvider.getSelectedDataset();
            InputStream sdf = cellDatasetProvider.getStructureIOClient().datasetExportToSdf(data, gzip);
            return sdf;
        }
    }
}
