package portal.notebook.service;

import toolkit.services.AbstractEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class MockAbstractNotebookVersion extends AbstractEntity {
    private MockNotebook mockNotebook;
    private MockNotebookEditable parent;
    private Date createdDate;
    private Date lastUpdatedDate;
    private byte[] json;

    @ManyToOne
    public MockNotebook getMockNotebook() {
        return mockNotebook;
    }

    public void setMockNotebook(MockNotebook mockNotebook) {
        this.mockNotebook = mockNotebook;
    }

    @ManyToOne
    public MockNotebookEditable getParent() {
        return parent;
    }

    public void setParent(MockNotebookEditable parent) {
        this.parent = parent;
    }

    public byte[] getJson() {
        return json;
    }

    public void setJson(byte[] json) {
        this.json = json;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
