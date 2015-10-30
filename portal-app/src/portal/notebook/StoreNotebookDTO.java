package portal.notebook;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StoreNotebookDTO {
    private NotebookMetadataDTO notebookMetadataDTO;
    private NotebookDefinitionDTO notebookDefinitionDTO;

    public NotebookMetadataDTO getNotebookMetadataDTO() {
        return notebookMetadataDTO;
    }

    public void setNotebookMetadataDTO(NotebookMetadataDTO notebookMetadataDTO) {
        this.notebookMetadataDTO = notebookMetadataDTO;
    }

    public NotebookDefinitionDTO getNotebookDefinitionDTO() {
        return notebookDefinitionDTO;
    }

    public void setNotebookDefinitionDTO(NotebookDefinitionDTO notebookDefinitionDTO) {
        this.notebookDefinitionDTO = notebookDefinitionDTO;
    }
}
