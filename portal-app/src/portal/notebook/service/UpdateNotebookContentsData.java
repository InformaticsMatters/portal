package portal.notebook.service;

import portal.notebook.api.NotebookInstance;

import java.io.Serializable;

public class UpdateNotebookContentsData implements Serializable {
    private Long id;
    private NotebookInstance notebookInstance;

    public NotebookInstance getNotebookInstance() {
        return notebookInstance;
    }

    public void setNotebookInstance(NotebookInstance notebookInstance) {
        this.notebookInstance = notebookInstance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
