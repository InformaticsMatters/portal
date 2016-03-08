package toolkit.services;

import com.sun.jersey.api.client.WebResource;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

@RequestScoped
public class ServiceSecurityContext {

    public static final String SERVICE_USERNAME = "Service-Username";
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void loadHeadersFromRequest(HttpHeaders httpHeaders) {
        MultivaluedMap<String, String> rh = httpHeaders.getRequestHeaders();
        setUsername(rh.getFirst(SERVICE_USERNAME));
    }

    public WebResource.Builder setHeadersToResource(WebResource webResource, Map<String, String> headers) {
        WebResource.Builder result = webResource.getRequestBuilder();
        for (String key : headers.keySet()) {
            result = webResource.header(key, headers.get(key));

            System.out.println("key: " + key + ", value: " + headers.get(key));
        }
        return result;
    }
}
