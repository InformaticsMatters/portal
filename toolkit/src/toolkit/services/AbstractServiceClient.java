package toolkit.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public abstract class AbstractServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServiceClient.class.getName());
    @Inject
    private ServiceContext serviceContext;
    private Client client;

    protected WebResource.Builder newResourceBuilder(String context, MultivaluedMap<String, String> queryParams) {
        String uriString = getServiceBaseUri() + context;
        UriBuilder builder = UriBuilder.fromUri(uriString);
        if (queryParams != null) {
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                builder = UriBuilder.fromUri(builder.queryParam(entry.getKey(), "{value}").build(entry.getValue().get(0)));
            }
        }

        URI uri = builder.build();

        logger.debug(uriString);

        if (client == null) {
            prepareClient();
        }
        WebResource resource = client.resource(uri);
        return serviceContext.copyContextToHeaders(resource);
    }

    protected WebResource.Builder newResourceBuilder(String context) {
        return newResourceBuilder(context, null);
    }

    protected synchronized void prepareClient() {
        if (client == null) {
            DefaultClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getClasses().add(JacksonJsonProvider.class);
            client = Client.create(clientConfig);
            client.setChunkedEncodingSize(4096);
        }
    }

    protected abstract String getServiceBaseUri();
}
