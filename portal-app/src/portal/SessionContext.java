package portal;

import org.apache.wicket.request.cycle.RequestCycle;
import org.squonk.security.DefaultUserDetailsManager;
import org.squonk.security.UserDetails;
import org.squonk.security.impl.KeycloakUserDetailsManager;

import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * @author simetrias
 */
@SessionScoped
public class SessionContext implements Serializable {

    private final KeycloakUserDetailsManager defaultUserDetailsManager = new KeycloakUserDetailsManager();

    public UserDetails getLoggedInUserDetails() {
        return defaultUserDetailsManager.getAuthenticatedUser((HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest());
    }

}
