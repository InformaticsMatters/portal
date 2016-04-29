package portal.notebook.webapp;

import org.squonk.notebook.api.AbstractNotebookVersionDTO;
import org.squonk.notebook.api.NotebookSavepointDTO;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HistoryNode implements Serializable {
    private Long id;
    private Long parentId;
    private String label;
    private final List<HistoryNode> childList = new ArrayList<>();
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<HistoryNode> getChildList() {
        return childList;
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
            node.setLabel(((NotebookSavepointDTO)dto).getDescription());
        } else {
            node.setLabel(dto.getId() + " - " + dateFormat.format(dto.getCreatedDate()));
        }
        node.setParentId(dto.getParentId());
        node.setNodeType(dto.getClass().getSimpleName());
        return node;
    }

}
