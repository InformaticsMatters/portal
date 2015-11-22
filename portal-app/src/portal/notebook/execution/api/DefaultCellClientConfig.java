package portal.notebook.execution.api;

import javax.enterprise.inject.Alternative;

@Alternative
public class DefaultCellClientConfig implements CellClientConfig {
    @Override
    public String getServiceBaseUri() {
        return "http://localhost:8080/ws/cell";
    }
}
