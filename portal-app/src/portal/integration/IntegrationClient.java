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
        return "http://52.0.104.20:8080/chemcentral";
    }

    /*
    propertyDefinitions?filter=adenosine&limit=20
     */
    public List<PropertyDefinition> listPropertyDefinition(String filter, int limit) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("filter", filter);
        queryParams.add("limit", Integer.toString(limit));
        WebResource.Builder builder = newResourceBuilder("/propertyDefinitions", queryParams);
        GenericType<List<PropertyDefinition>> gt = new GenericType<List<PropertyDefinition>>() {
        };
        return builder.get(gt);
    }

    public List<Structure> listStructureByHitlist(int hitList) {
        WebResource.Builder builder = newResourceBuilder("/hitlists/" + Integer.toString(hitList) + "/structures");
        GenericType<List<Structure>> gt = new GenericType<List<Structure>>() {
        };
        return builder.get(gt);
    }

    public List<Hitlist> listHitlist() {
        WebResource.Builder builder = newResourceBuilder("/hitlists");
        GenericType<List<Hitlist>> gt = new GenericType<List<Hitlist>>() {
        };
        return builder.get(gt);
    }

}
