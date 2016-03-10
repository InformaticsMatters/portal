package portal;

import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@SessionScoped
@Default
public class DefaultLogoutHandler implements LogoutHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLogoutHandler.class);

    @Inject
    private SessionContext sessionContext;


    @Override
    public void logout() {
        try {
            HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();

            String logoutURL = sessionContext.getLogoutURL();
            if (logoutURL == null) {
                logoutURL = "/";
            }
            sessionContext.clearLoginSession(request.getSession());
            logger.info("Logging out via " + logoutURL);
            RequestCycle.get().setResponsePage(new RedirectPage(logoutURL));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
