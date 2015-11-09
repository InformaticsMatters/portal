package portal.notebook.service;

import portal.notebook.api.NotebookDTO;
import portal.notebook.api.NotebookMetadataDTO;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StoreNotebookDTO {
    private NotebookMetadataDTO notebookMetadataDTO;
    private NotebookDTO notebookDTO;

    public NotebookMetadataDTO getNotebookMetadataDTO() {
        return notebookMetadataDTO;
    }

    public void setNotebookMetadataDTO(NotebookMetadataDTO notebookMetadataDTO) {
        this.notebookMetadataDTO = notebookMetadataDTO;
    }

    public NotebookDTO getNotebookDTO() {
        return notebookDTO;
    }

    public void setNotebookDTO(NotebookDTO notebookDTO) {
        this.notebookDTO = notebookDTO;
    }
}
