package portal.notebook.webapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.squonk.notebook.api.AbstractNotebookVersionDTO;
import org.squonk.notebook.api.NotebookSavepointDTO;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class HistoryNode implements Serializable {

    private final List<HistoryNode> children = new ArrayList<>();
    private Long id;
    private Long parentId;
    private String name;
    private String dateCreated;
    private String dateUpdated;
    private String nodeType;

    public static HistoryNode fromVersionDto(AbstractNotebookVersionDTO dto, DateFormat dateFormat) {
        HistoryNode node = new HistoryNode();
        node.setId(dto.getId());
        if (dto instanceof NotebookSavepointDTO) {
            NotebookSavepointDTO savepointDTO = (NotebookSavepointDTO) dto;
            node.setName(savepointDTO.getDescription());
        }
        node.setParentId(dto.getParentId());
        node.setNodeType(dto.getClass().getSimpleName());
        node.setDateCreated(dateFormat.format(dto.getCreatedDate()));
        node.setDateUpdated(dateFormat.format(dto.getLastUpdatedDate()));
        return node;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<HistoryNode> getChildren() {
        return children;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
}
