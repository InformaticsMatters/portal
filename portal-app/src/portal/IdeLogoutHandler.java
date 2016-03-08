package portal;

import org.apache.wicket.request.cycle.RequestCycle;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.servlet.http.HttpServletRequest;

@SessionScoped
@Alternative
public class IdeLogoutHandler implements LogoutHandler {
    @Override
    public void logout() {
        HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
        request.getSession().invalidate();
        RequestCycle.get().setResponsePage(LogoutPage.class);
    }
}
