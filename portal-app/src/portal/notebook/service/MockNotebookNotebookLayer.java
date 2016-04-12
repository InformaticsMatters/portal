package portal.notebook.service;

import toolkit.services.AbstractEntity;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"mocknotebook_id", "layername"})})
public class MockNotebookNotebookLayer extends AbstractEntity {
    private MockNotebook mockNotebook;
    private String layerName;

    @ManyToOne
    @JoinColumn(nullable = false)
    public MockNotebook getMockNotebook() {
        return mockNotebook;
    }

    public void setMockNotebook(MockNotebook mockNotebook) {
        this.mockNotebook = mockNotebook;
    }

    @Column(nullable = false)
    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
}
