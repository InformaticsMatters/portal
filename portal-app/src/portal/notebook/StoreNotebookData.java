package portal.notebook;

import portal.notebook.NotebookContents;
import portal.notebook.NotebookInfo;

import java.io.Serializable;

public class StoreNotebookData implements Serializable {
    private NotebookInfo notebookInfo;
    private NotebookContents notebookContents;

    public NotebookInfo getNotebookInfo() {
        return notebookInfo;
    }

    public void setNotebookInfo(NotebookInfo notebookInfo) {
        this.notebookInfo = notebookInfo;
    }

    public NotebookContents getNotebookContents() {
        return notebookContents;
    }

    public void setNotebookContents(NotebookContents notebookContents) {
        this.notebookContents = notebookContents;
    }
}
