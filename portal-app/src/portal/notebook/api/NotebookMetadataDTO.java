package portal.notebook.api;

import portal.notebook.service.Notebook;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NotebookMetadataDTO {
    private Long id;
    private String name;
    private String ownerName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void fromNotebook(Notebook notebook) {
        id = notebook.getId();
        name = notebook.getName();
    }
}
