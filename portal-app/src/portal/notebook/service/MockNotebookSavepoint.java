package portal.notebook.service;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class MockNotebookSavepoint extends MockAbstractNotebookVersion {
    private String creator;
    private String label;
    private String description;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Column(nullable = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(nullable = false)
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
