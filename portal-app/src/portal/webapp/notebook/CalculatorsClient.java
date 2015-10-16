package portal.webapp.notebook;


import com.im.lac.types.io.JsonHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Logger;

public class CalculatorsClient implements Serializable {
    private static final Logger LOG = Logger.getLogger(CalculatorsClient.class.getName());
    private static final String DEFAULT_BASE_URL = "http://demos.informaticsmatters.com:9080/chem-services-chemaxon-basic/rest/v1/calculators/";
    private final String base;
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final JsonHandler jsonHandler = new JsonHandler();

    public CalculatorsClient(String baseUrl) {
        this.base = baseUrl;
    }

    public CalculatorsClient() {
        this.base = DEFAULT_BASE_URL;
    }


    public void calculate(String calculatorName, InputStream inputStream, OutputStream outputStream) {
        HttpPost httpPost = new HttpPost(this.base + calculatorName);
        httpPost.setEntity(new InputStreamEntity(inputStream));
        try {
            CloseableHttpResponse response = this.httpclient.execute(httpPost);
            InputStream responseStream = response.getEntity().getContent();
            try {
                transfer(responseStream, outputStream);
            } finally {
                responseStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void transfer(InputStream responseStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int r = responseStream.read(buffer, 0, buffer.length);
        while (r > -1) {
            outputStream.write(buffer, 0, r);
            r = responseStream.read(buffer, 0, buffer.length);
        }
    }


}
