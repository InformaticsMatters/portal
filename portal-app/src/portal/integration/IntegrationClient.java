package portal.integration;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import toolkit.services.AbstractServiceClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 * @author simetrias
 */
@ApplicationScoped
public class IntegrationClient extends AbstractServiceClient {

    @Override
    protected String getServiceBaseUri() {
        return "http://demos.informaticsmatters.com:8080/chemcentral";
    }

    /*
    propertyDefinitions?filter=adenosine&limit=20
     */
    public List<PropertyDefinition> propertyDefinitions(String filter, int limit) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("filter", filter);
        queryParams.add("limit", Integer.toString(limit));
        WebResource.Builder builder = newResourceBuilder("/propertyDefinitions", queryParams);
        GenericType<List<PropertyDefinition>> gt = new GenericType<List<PropertyDefinition>>() {
        };
        return builder.get(gt);
    }
}
