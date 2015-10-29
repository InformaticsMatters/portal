package portal;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 * @author simetrias
 */
@SessionScoped
public class SessionContext implements Serializable {

    private String loggedInUser;

    public String getLoggedInUser() {
        return loggedInUser;
    }

    public boolean isLoggedInUser() {
        return loggedInUser != null;
    }

    public void setLoggedInUser(String loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
