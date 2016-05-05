package portal.notebook.service;

import toolkit.services.AbstractEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class MockNotebookVersion extends AbstractEntity {
    private MockNotebook mockNotebook;
    private String owner;
    private MockNotebookVersion parent;
    private Date createdDate;
    private Date lastUpdatedDate;
    private Boolean editable;
    private byte[] json;

    @ManyToOne
    @JoinColumn(nullable = false)
    public MockNotebook getMockNotebook() {
        return mockNotebook;
    }

    public void setMockNotebook(MockNotebook mockNotebook) {
        this.mockNotebook = mockNotebook;
    }

    @Column(nullable = false)
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @ManyToOne
    public MockNotebookVersion getParent() {
        return parent;
    }

    public void setParent(MockNotebookVersion parent) {
        this.parent = parent;
    }

    @Column(nullable = false)
    public byte[] getJson() {
        return json;
    }

    public void setJson(byte[] json) {
        this.json = json;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    @Column(nullable = false)
    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }
}
