package portal.notebook.webapp;

import org.squonk.notebook.api.AbstractNotebookVersionDTO;
import org.squonk.notebook.api.NotebookSavepointDTO;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;


public class HistoryNode implements Serializable {
    private Long id;
    private Long parentId;
    private String name;
    private final List<HistoryNode> children = new ArrayList<>();
    private String nodeType;

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

    public List<HistoryNode> getChildren() {
        return children;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public static HistoryNode fromVersionDto(AbstractNotebookVersionDTO dto, DateFormat dateFormat) {
        HistoryNode node = new HistoryNode();
        node.setId(dto.getId());
        if (dto instanceof NotebookSavepointDTO) {
            NotebookSavepointDTO savepointDTO = (NotebookSavepointDTO) dto;
            node.setName(savepointDTO.getDescription() == null ? buildDefaultNodeLabel(dto, dateFormat) : savepointDTO.getDescription());
        } else {
            node.setName(buildDefaultNodeLabel(dto, dateFormat));
        }
        node.setParentId(dto.getParentId());
        node.setNodeType(dto.getClass().getSimpleName());
        return node;
    }

    private static String buildDefaultNodeLabel(AbstractNotebookVersionDTO dto, DateFormat dateFormat) {
        return dto.getId() + " - " + dateFormat.format(dto.getCreatedDate());
    }

}
