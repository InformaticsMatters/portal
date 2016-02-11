package portal.notebook.service;

import portal.notebook.api.NotebookInstance;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class NotebookInstances {
    private final Map<Long, NotebookInstance> notebookInstanceMap = new HashMap<>();

    public NotebookInstance findNotebookInstance(Long id) {
        return notebookInstanceMap.get(id);
    }

    public void registerNotebookInstance(Long id, NotebookInstance notebookInstance) {
        synchronized (notebookInstanceMap) {
            notebookInstanceMap.put(id, notebookInstance);
        }
    }


    public void unregisterNotebookInstance(Long id) {
        synchronized (notebookInstanceMap) {
            notebookInstanceMap.remove(id);
        }
    }
}
