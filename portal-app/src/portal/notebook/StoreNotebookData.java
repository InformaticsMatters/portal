package portal.notebook;

import java.io.Serializable;

public class StoreNotebookData implements Serializable {
    private NotebookInfo notebookInfo;
    private NotebookModel notebookModel;

    public NotebookInfo getNotebookInfo() {
        return notebookInfo;
    }

    public void setNotebookInfo(NotebookInfo notebookInfo) {
        this.notebookInfo = notebookInfo;
    }

    public NotebookModel getNotebookModel() {
        return notebookModel;
    }

    public void setNotebookModel(NotebookModel notebookModel) {
        this.notebookModel = notebookModel;
    }
}
