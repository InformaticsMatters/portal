package portal.notebook.webapp;

import org.squonk.notebook.api.NotebookDTO;

import java.io.Serializable;


public class NotebookInfo implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Boolean shared;
    private String owner;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public static NotebookInfo fromNotebookDescriptor(NotebookDTO notebookDescriptor) {
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setId(notebookDescriptor.getId());
        notebookInfo.setName(notebookDescriptor.getName());
        notebookInfo.setDescription(notebookDescriptor.getDescription());
        notebookInfo.setOwner(notebookDescriptor.getOwner());
        notebookInfo.setShared(false);
        return notebookInfo;
    }


}
