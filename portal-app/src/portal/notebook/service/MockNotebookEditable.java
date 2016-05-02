package portal.notebook.service;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class MockNotebookEditable extends MockAbstractNotebookVersion {
    private String owner;

    @Column(nullable = false)
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
