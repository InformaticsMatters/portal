package portal.notebook.service;

import portal.notebook.api.NotebookClientConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;


@Alternative
@ApplicationScoped
public class IdeNotebookClientConfig implements NotebookClientConfig {
    @Override
    public String getBaseUri() {
        return "http://localhost:8080/ws/notebook";
    }
}
