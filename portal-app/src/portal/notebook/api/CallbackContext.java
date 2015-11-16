package portal.notebook.api;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class CallbackContext {
    private Long notebookId;
    private String cellName;

    public Long getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(Long notebookId) {
        this.notebookId = notebookId;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }
}
