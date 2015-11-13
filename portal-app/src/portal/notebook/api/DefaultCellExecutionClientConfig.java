package portal.notebook.api;

import javax.enterprise.inject.Alternative;

@Alternative
public class DefaultCellExecutionClientConfig implements CellExecutionClientConfig {
    @Override
    public String getBaseUri() {
        return "http://localhost:8080/ws/cell";
    }
}