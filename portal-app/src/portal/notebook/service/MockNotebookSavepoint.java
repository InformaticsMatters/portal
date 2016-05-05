package portal.notebook.service;

import toolkit.services.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class MockNotebookSavepoint extends AbstractEntity {
    private MockNotebookVersion mockNotebookVersion;
    private String creator;
    private String label;
    private String description;

    @ManyToOne
    @JoinColumn(nullable = false)
    public MockNotebookVersion getMockNotebookVersion() {
        return mockNotebookVersion;
    }

    public void setMockNotebookVersion(MockNotebookVersion mockNotebookVersion) {
        this.mockNotebookVersion = mockNotebookVersion;
    }

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
