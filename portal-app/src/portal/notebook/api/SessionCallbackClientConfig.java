package portal.notebook.api;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;

@SessionScoped
@Alternative
public class SessionCallbackClientConfig implements CallbackClientConfig {
    @Override
    public String getBaseUri() {
        return null;
    }
}
