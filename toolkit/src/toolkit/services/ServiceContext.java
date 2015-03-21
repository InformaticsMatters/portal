package toolkit.services;

import com.sun.jersey.api.client.WebResource;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

@RequestScoped
public class ServiceContext {

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void copyHeadersToContext(HttpHeaders httpHeaders) {
        MultivaluedMap<String, String> rh = httpHeaders.getRequestHeaders();
        setUsername(rh.getFirst(WebServiceHeaders.SERVICE_USERNAME));
    }

    public WebResource.Builder copyContextToHeaders(WebResource webResource) {
        return webResource.header(WebServiceHeaders.SERVICE_USERNAME, username);
    }

}
