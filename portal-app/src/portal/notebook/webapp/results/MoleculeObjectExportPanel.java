package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import portal.notebook.webapp.AbstractCellDatasetProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by timbo on 02/09/16.
 */
public class MoleculeObjectExportPanel extends Panel {


    private final AbstractCellDatasetProvider cellDatasetProvider;

    public MoleculeObjectExportPanel(String id, AbstractCellDatasetProvider cellDatasetProvider) {
        super(id);
        this.cellDatasetProvider = cellDatasetProvider;

        // results as json
        String cellName = cellDatasetProvider.getCellInstance().getName();

        ResourceLink resultsLink = new ResourceLink("resultsLink", new ResultsExportResource(cellName + "_results.json", false));
        resultsLink.add(new Label("resultsFilename", cellName + ".json"));
        add(resultsLink);

        ResourceLink resultsgzLink = new ResourceLink("resultsgzLink", new ResultsExportResource(cellName + "_results.json.gz", true));
        resultsgzLink.add(new Label("resultsgzFilename", cellName + ".json.gz"));
        add(resultsgzLink);


        // metadata as json
        ResourceLink metadataLink = new ResourceLink("metadataLink", new MetadataExportResource(cellName + "_metadata.json", false));
        metadataLink.add(new Label("metadataFilename", cellName + "_metadata.json"));
        add(metadataLink);

        ResourceLink metadatagzLink = new ResourceLink("metadatagzLink", new MetadataExportResource(cellName + "_metadata.json.gz", true));
        metadatagzLink.add(new Label("metadatagzFilename", cellName + "_metadata.json.gz"));
        add(metadatagzLink);


        // results as SDF
        ResourceLink sdfLink = new ResourceLink("sdfLink", new SDFExportResource(cellName + ".sdf", false));
        sdfLink.add(new Label("sdfFilename", cellName + ".sdf"));
        add(sdfLink);

        ResourceLink sdfgzLink = new ResourceLink("sdfgzLink", new SDFExportResource(cellName + ".sdf.gz", true));
        sdfgzLink.add(new Label("sdfgzFilename", cellName + ".sdf.gz"));
        add(sdfgzLink);
    }


    abstract class DatasetExportResource extends AbstractResource {

        String filename;
        boolean gzip;

        DatasetExportResource(String filename, boolean gzip) {
            this.filename = filename;
            this.gzip = gzip;
        }

        abstract InputStream fetchResults(boolean gzip) throws Exception;

        @Override
        protected ResourceResponse newResourceResponse(Attributes attributes) {

            ResourceResponse resp = new ResourceResponse();
            resp.setContentType(CommonMimeTypes.MIME_TYPE_MDL_SDF);
            resp.setContentDisposition(ContentDisposition.ATTACHMENT);
            resp.setFileName(filename);

            resp.setWriteCallback(new WriteCallback() {
                @Override
                public void writeData(Attributes attributes) throws IOException {

                    OutputStream out = null;
                    try (InputStream results = fetchResults(false)) {

                        Response response = attributes.getResponse();
                        out = response.getOutputStream();
                        if (gzip) {
                            out = new GZIPOutputStream(out);
                        }

                        byte[] buffer = new byte[1024];
                        while (true) {
                            int rsz = results.read(buffer);
                            if (rsz < 0) {
                                break;
                            }
                            out.write(buffer, 0, rsz);
                        }
                    } catch (IOException ioe) {
                        throw ioe;
                    } catch (Exception e) {
                        throw new IOException("Export failed", e);
                    } finally {
                        if (out != null) {
                            IOUtils.close(out);
                        }
                    }
                }
            });
            return resp;
        }
    }

    class ResultsExportResource extends DatasetExportResource {

        ResultsExportResource(String filename, boolean gzip) {
            super(filename, gzip);
        }

        InputStream fetchResults(boolean gzip) throws Exception {
            return cellDatasetProvider.getSelectedDataset().getInputStream(gzip);
        }
    }

    class MetadataExportResource extends DatasetExportResource {

        MetadataExportResource(String filename, boolean gzip) {
            super(filename, gzip);
        }

        @Override
        InputStream fetchResults(boolean gzip) throws Exception {
            DatasetMetadata meta = cellDatasetProvider.getSelectedMetadata();
            String json = JsonHandler.getInstance().objectToJson(meta);
            InputStream is = new ByteArrayInputStream(json.getBytes());
            return gzip ? new GZIPInputStream(is) : is;
        }
    }

    class SDFExportResource extends DatasetExportResource {

        SDFExportResource(String filename, boolean gzip) {
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
