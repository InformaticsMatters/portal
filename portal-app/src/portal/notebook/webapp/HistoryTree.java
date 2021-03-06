package portal.notebook.webapp;

import org.squonk.notebook.api.AbstractNotebookVersionDTO;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class HistoryTree implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(HistoryTree.class.getName());
    private final transient DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH':'mm':'ss");
    private String name;
    private final List<HistoryNode> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<HistoryNode> getChildren() {
        return children;
    }

    public void loadVersionMap(Map<Long, AbstractNotebookVersionDTO> versionMap) {
        Map<Long, HistoryNode> nodeMap = new HashMap<>();
        for (Long id : versionMap.keySet()) {
            AbstractNotebookVersionDTO dto = versionMap.get(id);
            LOGGER.fine("Adding to tree: id:" + dto.getId() + " parent: " + dto.getParentId() + " type: " + dto.getClass().getName());
            HistoryNode node = HistoryNode.fromVersionDto(dto, dateFormat);
            nodeMap.put(node.getId(), node);
        }
        for (HistoryNode node : nodeMap.values()) {
            if (node.getParentId() == null) {
                children.add(node);
            } else {
                HistoryNode parentNode = nodeMap.get(node.getParentId());
                parentNode.getChildren().add(node);
            }
        }
    }

    public String toTreeString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name).append("\r\n");
        for (HistoryNode node : children) {
            nodeToTreeString(node, stringBuilder, 1);
        }
        return stringBuilder.toString();
    }

    private void nodeToTreeString(HistoryNode node, StringBuilder stringBuilder, int level) {
        for (int i = 0; i < level; i ++) {
            stringBuilder.append("   ");
        }
        stringBuilder.append(node.getName());
        stringBuilder.append("\r\n");
        for (HistoryNode child : node.getChildren()) {
            nodeToTreeString(child, stringBuilder, level + 1);
        }
    }


}
