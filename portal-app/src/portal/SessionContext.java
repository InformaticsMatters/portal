package portal;

import org.apache.wicket.request.cycle.RequestCycle;
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
    private boolean firstTime = true;

    public UserDetails getLoggedInUserDetails() {
        UserDetails result = defaultUserDetailsManager.getAuthenticatedUser((HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest());
        if (firstTime) {
            runOnce(result);
            firstTime = false;
        }
        return result;
    }

    private void runOnce(UserDetails userDetails) {
        // perform tasks needed when this user session first activates
    }
}
