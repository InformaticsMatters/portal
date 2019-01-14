package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import portal.notebook.webapp.AbstractCellDatasetProvider;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Export panel for BasicObjects.
 * Types that extend BasicObject can extend this class and specify custom formats
 * <p>
 * Created by timbo on 10/04/18.
 */
public class BasicObjectExportPanel extends Panel {

    private static final Logger LOG = Logger.getLogger(BasicObjectExportPanel.class.getName());


    protected final AbstractCellDatasetProvider cellDatasetProvider;

    public BasicObjectExportPanel(String id, AbstractCellDatasetProvider cellDatasetProvider) {
        super(id);
        this.cellDatasetProvider = cellDatasetProvider;

        // results as json
        String cellName = cellDatasetProvider.getCellInstance().getName();

        ResourceLink resultsLink = new ResourceLink("resultsLink",
                new ResultsJsonExportResource(cellName + "_results.json", false));
        resultsLink.add(new Label("resultsFilename", cellName + ".json"));
        add(resultsLink);

        ResourceLink resultsgzLink = new ResourceLink("resultsgzLink",
                new ResultsJsonExportResource(cellName + "_results.json.gz", true));
        resultsgzLink.add(new Label("resultsgzFilename", cellName + ".json.gz"));
        add(resultsgzLink);

        // metadata as json
        ResourceLink metadataLink = new ResourceLink("metadataLink",
                new MetadataJsonExportResource(cellName + "_metadata.json", false));
        metadataLink.add(new Label("metadataFilename", cellName + "_metadata.json"));
        add(metadataLink);

        ResourceLink metadatagzLink = new ResourceLink("metadatagzLink",
                new MetadataJsonExportResource(cellName + "_metadata.json.gz", true));
        metadatagzLink.add(new Label("metadatagzFilename", cellName + "_metadata.json.gz"));
        add(metadatagzLink);

        // data as csv
        ResourceLink csvLink = new ResourceLink("csvLink",
                new CSVExportResource(cellName + ".csv", false, ResultsUtils.ExportFormat.CSV));
        csvLink.add(new Label("csvFilename", cellName + ".csv"));
        add(csvLink);

        ResourceLink csvgzLink = new ResourceLink("csvgzLink",
                new CSVExportResource(cellName + ".csv.gz", true, ResultsUtils.ExportFormat.CSV));
        csvgzLink.add(new Label("csvgzFilename", cellName + "csv.gz"));
        add(csvgzLink);

        // data as tsv
        ResourceLink tsvLink = new ResourceLink("tsvLink",
                new CSVExportResource(cellName + ".tab", false, ResultsUtils.ExportFormat.TSV));
        tsvLink.add(new Label("tsvFilename", cellName + ".tab"));
        add(tsvLink);

        ResourceLink tsvgzLink = new ResourceLink("tsvgzLink",
                new CSVExportResource(cellName + ".tab.gz", true, ResultsUtils.ExportFormat.TSV));
        tsvgzLink.add(new Label("tsvgzFilename", cellName + "tab.gz"));
        add(tsvgzLink);
    }

    abstract class AbstractDatasetExportResource extends AbstractResource {

        String filename;
        boolean gzip;

        AbstractDatasetExportResource(String filename, boolean gzip) {
            this.filename = filename;
            this.gzip = gzip;
        }

        Dataset fetchDataset(boolean gzip) throws Exception {
            return cellDatasetProvider.getSelectedDataset();
        }

    }


    abstract class AbstractDatasetJsonExportResource extends AbstractDatasetExportResource {

        AbstractDatasetJsonExportResource(String filename, boolean gzip) {
            super(filename, gzip);
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
                    try (InputStream results = fetchResults(gzip)) {

                        LOG.fine("Fetched results: gzip:" + gzip + " " + results);

                        Response response = attributes.getResponse();
                        out = response.getOutputStream();

                        byte[] buffer = new byte[4096];
                        int count = 0;
                        while (true) {
                            count++;
                            LOG.finer("Reading " + count);
                            int rsz = results.read(buffer);
                            if (rsz < 0) {
                                LOG.fine("Reading complete");
                                break;
                            }
                            LOG.finer("Writing response " + count + " " + rsz);
                            out.write(buffer, 0, rsz);
                            out.flush();
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

    class ResultsJsonExportResource extends AbstractDatasetJsonExportResource {

        ResultsJsonExportResource(String filename, boolean gzip) {
            super(filename, gzip);
        }

        InputStream fetchResults(boolean gzip) throws Exception {
            return cellDatasetProvider.getSelectedDataset().getInputStream(gzip);
        }
    }

    class MetadataJsonExportResource extends AbstractDatasetJsonExportResource {

        MetadataJsonExportResource(String filename, boolean gzip) {
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

    class CSVExportResource extends AbstractDatasetExportResource {

        private final ResultsUtils.ExportFormat format;

        CSVExportResource(String filename, boolean gzip, ResultsUtils.ExportFormat format) {
            super(filename, gzip);
            this.format = format;
        }

        @Override
        protected ResourceResponse newResourceResponse(Attributes attributes) {

            ResourceResponse resp = new ResourceResponse();
            resp.setContentType(CommonMimeTypes.MIME_TYPE_MDL_SDF);
            resp.setContentDisposition(ContentDisposition.ATTACHMENT);
            resp.setFileName(filename);

            resp.setWriteCallback(new WriteCallback() {
                @Override
                public void writeData(Attributes attributes) throws IOException {

                    OutputStreamWriter writer = null;
                    try {

                        Dataset<BasicObject> dataset = fetchDataset(gzip);
                        Response response = attributes.getResponse();
                        OutputStream out = response.getOutputStream();
                        if (gzip) {
                            out = new GZIPOutputStream(out);
                        }
                        writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));

                        Stream<String> lines = ResultsUtils.convertDatasetToText(dataset, format, true);

                        Iterator<String> it = lines.iterator();
                        while (it.hasNext()) {
                            String line = it.next();
                            writer.write(line);
                            writer.write("\n");
                        }

                    } catch (IOException ioe) {
                        throw ioe;
                    } catch (Exception e) {
                        throw new IOException("Export failed", e);
                    } finally {
                        if (writer != null) {
                            IOUtils.close(writer);
                        }
                    }
                }
            });
            return resp;
        }
    }

}
