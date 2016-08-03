package portal.notebook.service;

import toolkit.services.AbstractEntity;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"mocknotebookeditable_id", "cellid", "name"})})
public class MockVariable extends AbstractEntity {
    private MockNotebookVersion mockNotebookVersion;
    private Long cellId;
    private String name;
    private String value;
    private byte[] streamValue;

    @ManyToOne
    @JoinColumn(nullable = false)
    public MockNotebookVersion getMockNotebookVersion() {
        return mockNotebookVersion;
    }

    public void setMockNotebookVersion(MockNotebookVersion mockNotebookVersion) {
        this.mockNotebookVersion = mockNotebookVersion;
    }

    @Column(length = 4000)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(nullable = false)
    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Lob
    public byte[] getStreamValue() {
        return streamValue;
    }

    public void setStreamValue(byte[] streamValue) {
        this.streamValue = streamValue;
    }
}
