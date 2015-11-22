package portal.notebook.execution.api;

import javax.enterprise.inject.Alternative;

@Alternative
public class DefaultCallbackClientConfig implements CallbackClientConfig {

    private final String url;

    public DefaultCallbackClientConfig() {
        String s = System.getenv("SERVICE_CALLBACK");
        if (s == null) {
            url = "http://localhost:8080/ws/callback";
        }   else {
            url = s;
        }
    }

    @Override
    public String getBaseUri() {
        return url;
    }
}
