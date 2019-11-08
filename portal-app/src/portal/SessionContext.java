package portal;

import org.apache.wicket.request.cycle.RequestCycle;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.client.UserClient;
import org.squonk.core.user.User;
import org.squonk.security.UserDetails;
import org.squonk.security.impl.KeycloakUserDetailsManager;
import org.squonk.util.IOUtils;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;

/**
 * @author simetrias
 */
@SessionScoped
public class SessionContext implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(SessionContext.class);
    private static final String LOGOUT_REDIRECT_TO = IOUtils.getConfiguration(
            "LOGOUT_REDIRECT_TO",
            IOUtils.getConfiguration("PUBLIC_HOST_URL", "http://localhost:8080"));


    @Inject
    private UserClient client;
    private final KeycloakUserDetailsManager defaultUserDetailsManager = new KeycloakUserDetailsManager();
    private boolean firstTime = true;

    public UserDetails getLoggedInUserDetails() {
        logger.debug("Getting UserDetails");
        UserDetails result = defaultUserDetailsManager.getAuthenticatedUser((HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest(), false);
        if (firstTime && result != null) {
            runOnce(result);
            firstTime = false;
        }
        return result;
    }

    private HttpServletRequest getRequest() {
        return (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
    }

    public AccessToken getAccessToken() {
        AccessToken token = defaultUserDetailsManager.getAccessToken(getRequest());
        return token;
    }

    public String getAuthorizationHeader() {
        return defaultUserDetailsManager.getAuthorizationHeader(getRequest());
    }

    public void clearLoginSession(HttpSession session) {
        logger.debug("Clearing session for user");
        defaultUserDetailsManager.clearUserDetailsFromSession(session);
        session.invalidate();
    }

    private void runOnce(UserDetails userDetails) {

        String authenticatedUser = userDetails.getUserid();
        if (authenticatedUser != null) {
            logger.info("Initial setup for user " + authenticatedUser);
            try {
                User user = client.getUser(authenticatedUser);
                if (user != null) {
                    logger.info("User " + user.getUsername() + " has ID " + user.getId());
                }
            } catch (Exception ioe) {
                logger.error("Failed to retrieve user object", ioe);
            }
        }
    }

    public String getLogoutURL() {
        try {
            HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
            URI logoutURI = defaultUserDetailsManager.getLogoutUrl(request, LOGOUT_REDIRECT_TO);
            if (logoutURI == null) {
                return null;
            } else {
                return logoutURI.toURL().toString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
