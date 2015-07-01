package portal.datamart;

import com.sun.jersey.api.client.ClientResponse;
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
public class DatamartClient extends AbstractServiceClient {

    @Override
    protected String getServiceBaseUri() {
        return "http://52.5.246.115:8080/chemcentral";
    }

    public List<PropertyDefinition> listPropertyDefinition(String filter, int limit) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("filter", filter);
        queryParams.add("limit", Integer.toString(limit));
        WebResource.Builder builder = newResourceBuilder("/propertyDefinitions", queryParams);
        GenericType<List<PropertyDefinition>> gt = new GenericType<List<PropertyDefinition>>() {
        };
        return builder.get(gt);
    }

    public List<Structure> listStructure(Long hitListId) {
        WebResource.Builder builder = newResourceBuilder("/hitlists/" + hitListId.toString() + "/structures");
        GenericType<List<Structure>> gt = new GenericType<List<Structure>>() {
        };
        return builder.get(gt);
    }

    public List<Structure> listStructure(List<Long> structureIdList) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        for (Long structureId : structureIdList) {
            queryParams.add("id", structureId.toString());
        }
        WebResource.Builder builder = newResourceBuilder("/structures", queryParams);
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

    public List<PropertyData> listPropertyData(Long hitListId, String originalPropertyId) {
        WebResource.Builder builder = newResourceBuilder("/hitlists/" + hitListId.toString() + "/properties/" + originalPropertyId);
        GenericType<List<PropertyData>> gt = new GenericType<List<PropertyData>>() {
        };
        return builder.get(gt);
    }

    public String createHitListWithDataFor(int sourceId, List<String> originalPropertyIdList) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("sourceId", Integer.toString(sourceId));
        for (String propertyDefinitionId : originalPropertyIdList) {
            queryParams.add("propertyId", propertyDefinitionId);
        }
        WebResource.Builder builder = newResourceBuilder("/hitlists/structures/data", queryParams);
        ClientResponse response = builder.post(ClientResponse.class);
        String resultUri = null;
        if (response.getStatus() == 201) {
            resultUri = response.getHeaders().getFirst("Location");
        }
        return resultUri;
    }

    public Hitlist loadHitlist(Long hitlistId) {
        WebResource.Builder builder = newResourceBuilder("/hitlists/" + hitlistId.toString());
        return builder.get(Hitlist.class);
    }
}
