package portal.notebook.webapp;

import org.squonk.notebook.api.NotebookDTO;

import java.io.Serializable;


public class NotebookInfo implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Boolean shared;
    private String owner;
    private Boolean shareable;

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

    public static NotebookInfo fromNotebookDTO(NotebookDTO notebookDTO) {
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setId(notebookDTO.getId());
        notebookInfo.setName(notebookDTO.getName());
        notebookInfo.setDescription(notebookDTO.getDescription());
        notebookInfo.setOwner(notebookDTO.getOwner());
        notebookInfo.setShared(notebookDTO.getLayers().contains("public"));
        notebookInfo.setShareable(notebookDTO.getSavepointCount() > 0);
        return notebookInfo;
    }


    public void setShareable(Boolean shareable) {
        this.shareable = shareable;
    }

    public Boolean getShareable() {
        return shareable;
    }
}
