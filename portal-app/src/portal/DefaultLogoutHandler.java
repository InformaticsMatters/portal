package portal;

import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@SessionScoped
@Default
public class DefaultLogoutHandler implements LogoutHandler {
    @Inject
    private SessionContext sessionContext;


    @Override
    public void logout() {
        try {
            HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
            request.getSession().invalidate();
            String logoutURL = sessionContext.getLogoutURL();
            if (logoutURL == null) {
                logoutURL = "/";
            }
            RequestCycle.get().setResponsePage(new RedirectPage(logoutURL));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
