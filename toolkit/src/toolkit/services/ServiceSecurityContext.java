package toolkit.services;

import com.sun.jersey.api.client.WebResource;
import org.squonk.security.UserDetailsManager;
import org.squonk.security.impl.KeycloakUserDetailsManager;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestScoped
public class ServiceSecurityContext {

    public static final String HEADER_USERNAME = "Service-Username";
    private UserDetailsManager userDetailsManager = new KeycloakUserDetailsManager();
    private String username;

    public void loadSecurityHeadersFromRequest(HttpServletRequest httpServletRequest) {
        setUsername(httpServletRequest.getHeader(HEADER_USERNAME));
        // set other properties using known security header keys
    }

    public WebResource.Builder setSecurityHeadersToResource(WebResource webResource, HttpServletRequest httpServletRequest) {
        WebResource.Builder result = webResource.getRequestBuilder();
        Map<String, String> headers = userDetailsManager.getSecurityHeaders(httpServletRequest);
        for (String key : headers.keySet()) {
            result = webResource.header(key, headers.get(key));
        }
        return result;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
