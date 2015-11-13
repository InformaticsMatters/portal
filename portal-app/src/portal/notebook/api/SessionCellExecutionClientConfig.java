package portal.notebook.api;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;

@SessionScoped
@Alternative
public class SessionCellExecutionClientConfig implements CellExecutionClientConfig {
    @Override
    public String getBaseUri() {
        return null;
    }
}
