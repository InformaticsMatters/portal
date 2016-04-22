package portal.notebook.service;

import portal.notebook.api.PortalClientConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;


@Alternative
@ApplicationScoped
public class IdePortalClientConfig implements PortalClientConfig {
    @Override
    public String getBaseUri() {
        return "http://localhost:8080/ws/portal";
    }
}
