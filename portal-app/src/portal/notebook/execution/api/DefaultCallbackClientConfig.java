package portal.notebook.execution.api;

import javax.enterprise.inject.Alternative;

@Alternative
public class DefaultCallbackClientConfig implements CallbackClientConfig {
    @Override
    public String getBaseUri() {
        return "http://localhost:8080/ws/callback";
    }
}
