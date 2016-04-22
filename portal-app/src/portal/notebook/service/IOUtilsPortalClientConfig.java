package portal.notebook.service;

import org.squonk.util.IOUtils;
import portal.notebook.api.PortalClientConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;


@Default
@ApplicationScoped
public class IOUtilsPortalClientConfig implements PortalClientConfig {

    private static final String BASE_URL = IOUtils.getConfiguration("SERVICE_CALLBACK", "http://localhost:8080") + "/ws/portal";

    @Override
    public String getBaseUri() {
        return BASE_URL;
    }
}
