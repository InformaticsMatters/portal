package portal.notebook.execution.api;

import javax.enterprise.inject.Alternative;

@Alternative
public class DefaultCellClientConfig implements CellClientConfig {

    private final String url;

    public DefaultCellClientConfig() {
        String s = System.getenv("SERVICE_CELL_EXECUTION");
        if (s == null) {
            url = "http://localhost:8080/ws/cell";
        }   else {
            url = s;
        }
    }

    @Override
    public String getServiceBaseUri() {
        return url;
    }
}
