package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.squonk.dataset.Dataset;
import org.squonk.types.BasicObject;
import org.squonk.types.depict.HTMLRenderers;
import org.squonk.types.io.JsonHandler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 05/10/16.
 */
public class ResultsUtils {

    private static final Logger LOG = Logger.getLogger(ResultsUtils.class.getName());
    private static final HTMLRenderers HTML = HTMLRenderers.getInstance();

    public enum ExportFormat {CSV, TSV}
    private static Map<ExportFormat, String> separators = new HashMap<>();
    static {
        separators.put(ExportFormat.CSV, ",");
        separators.put(ExportFormat.TSV, "\t");
    }

    public static WebComponent generateContent(String id, Object value) {
        if (value == null) {
            return new Label(id, "");
        } else if (HTML.canRender(value.getClass())) {
            Label l = new Label(id, HTML.render(value));
            l.setEscapeModelStrings(false);
            return l;
        } else {
            return new MultiLineLabel(id, value.toString());
        }
    }

    /** Convert the Dataset to a text representation.
     * Currently this just handles the data values (e.g. not the structures).
     *
     * @param dataset The dataset to convert
     * @param format The export format
     * @param includeHeader Include header line for formats like CSV and TSV
     * @return A Stream of lines, including any header and footer lines. Newline characters are NOT added.
     * @throws IOException
     */
    public static Stream<String> convertDatasetToText(Dataset<BasicObject> dataset, ExportFormat format, boolean includeHeader) throws IOException {

        Map<String,Class> mappings = new LinkedHashMap(dataset.getMetadata().getValueClassMappings());
        String sep = separators.get(format);

        // first deal with the header line if specified
        String header = null;
        if (includeHeader) {
            LOG.info("Writing header line");
            StringBuilder builder = new StringBuilder();
            int count = 0;
            for (Map.Entry<String,Class> e: mappings.entrySet()) {
                if (count > 0) {
                    builder.append(sep);
                }
                builder.append("\"");
                builder.append(e.getKey());
                builder.append("\"");
                count++;
            }
            header = builder.toString();
        }
        // now handle the data lines
        Stream<String> lines =  dataset.getStream().sequential().map(bo -> {
            StringBuilder builder = new StringBuilder();
            int count = 0;
            for (Map.Entry<String,Class> mapping: mappings.entrySet()) {
                if (count > 0) {
                    builder.append(sep);
                }
                Object value = bo.getValue(mapping.getKey());
                if (value != null) {
                    if (mapping.getValue() == String.class) {
                        builder.append("\"");
                        builder.append(value.toString());
                        builder.append("\"");
                    } else {
                        builder.append(value.toString());
                    }
                }
                count++;
            }
            return builder.toString();
        });

        // handle what is returned, including header line
        if (header == null) {
            return lines;
        } else {
            return Stream.concat(Stream.of(header), lines);
        }
    }

}
