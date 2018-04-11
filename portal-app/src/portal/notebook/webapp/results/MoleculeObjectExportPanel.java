package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import portal.notebook.webapp.AbstractCellDatasetProvider;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/** Export panel for MoleculeObjects.
 * This adds SDF export to the standaard formats provided by BasicObject
 *
 * Created by timbo on 02/09/16.
 */
public class MoleculeObjectExportPanel extends BasicObjectExportPanel {


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
            super(filename, gzip);
        }

        @Override
        @SuppressWarnings("unchecked")
        InputStream fetchResults(boolean gzip) throws Exception {
            InputStream sdf = cellDatasetProvider.getStructureIOClient().datasetExportToSdf(cellDatasetProvider.getSelectedDataset(), false);
            return gzip ? new GZIPInputStream(sdf) : sdf;
        }
    }
}
