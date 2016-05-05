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
    private String nodeType;

    public static HistoryNode fromVersionDto(AbstractNotebookVersionDTO dto, DateFormat dateFormat) {
        HistoryNode node = new HistoryNode();
        node.setId(dto.getId());
        if (dto.getParentId() == null) {
            node.setName("root");
        } else if (dto instanceof NotebookSavepointDTO) {
            NotebookSavepointDTO savepointDTO = (NotebookSavepointDTO) dto;
            node.setName(savepointDTO.getDescription() == null ? buildDefaultNodeDescription(dto, dateFormat) : savepointDTO.getDescription());
        } else {
            node.setName(buildDefaultNodeDescription(dto, dateFormat));
        }
        node.setParentId(dto.getParentId());
        node.setNodeType(dto.getClass().getSimpleName());
        return node;
    }

    private static String buildDefaultNodeDescription(AbstractNotebookVersionDTO dto, DateFormat dateFormat) {
        return dateFormat.format(dto.getCreatedDate());
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

}
