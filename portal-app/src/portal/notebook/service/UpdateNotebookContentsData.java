package portal.notebook.service;

import java.io.Serializable;

public class UpdateNotebookContentsData implements Serializable {
    private Long id;
    private NotebookContents notebookContents;

    public NotebookContents getNotebookContents() {
        return notebookContents;
    }

    public void setNotebookContents(NotebookContents notebookContents) {
        this.notebookContents = notebookContents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
