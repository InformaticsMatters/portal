package portal.notebook.service;

import javax.persistence.Entity;

@Entity
public class MockNotebookSavepoint extends MockNotebookEditable {
    private Long editableId;
    private String label;
    private String description;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getEditableId() {
        return editableId;
    }

    public void setEditableId(Long editableId) {
        this.editableId = editableId;
    }
}
