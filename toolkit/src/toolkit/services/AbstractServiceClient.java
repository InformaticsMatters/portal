package toolkit.services;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.security.UserDetailsManager;
import org.squonk.security.impl.KeycloakUserDetailsManager;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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
    private UserDetailsManager userDetailsManager = new KeycloakUserDetailsManager();
    private Client jerseyClient;
    @Inject
    private ServiceSecurityContext serviceSecurityContext;
    @Inject
    private HttpServletRequest servletRequest;

    protected WebResource.Builder newResourceBuilder(String context, MultivaluedMap<String, String> queryParams) {
        String uriString = getServiceBaseUri() + context;
        UriBuilder builder = UriBuilder.fromUri(uriString);
        if (queryParams != null) {
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                for (String value : entry.getValue()) {
                    builder = UriBuilder.fromUri(builder.queryParam(entry.getKey(), "{value}").build(value));
                }
            }
        }

        URI uri = builder.build();

        logger.debug(uriString);

        if (jerseyClient == null) {
            prepareClient();
        }
        WebResource resource = jerseyClient.resource(uri);
        Map<String, String> headers = userDetailsManager.getSecurityHeaders(servletRequest);
        return serviceSecurityContext.setHeadersToResource(resource, headers);
    }

    protected WebResource.Builder newResourceBuilder(String context) {
        return newResourceBuilder(context, null);
    }

    protected synchronized void prepareClient() {
        if (jerseyClient == null) {
            DefaultClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getClasses().add(JacksonJsonProvider.class);
            jerseyClient = Client.create(clientConfig);
            jerseyClient.setChunkedEncodingSize(4096);
        }
    }

    protected abstract String getServiceBaseUri();


}
