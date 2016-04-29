package portal.notebook.webapp;

import org.squonk.notebook.api.AbstractNotebookVersionDTO;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryTree implements Serializable {
    private final transient DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH':'mm':'ss");
    private String name;
    private final List<HistoryNode> nodeList = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<HistoryNode> getNodeList() {
        return nodeList;
    }

    public void loadVersionMap(Map<Long, AbstractNotebookVersionDTO> versionMap) {
        Map<Long, HistoryNode> nodeMap = new HashMap<>();
        for (Long id : versionMap.keySet()) {
            AbstractNotebookVersionDTO dto = versionMap.get(id);
            HistoryNode node = HistoryNode.fromVersionDto(dto, dateFormat);
            nodeMap.put(node.getId(), node);
            if (dto.getParentId() == null) {
                nodeList.add(node);
                nodeMap.put(node.getId(), node);
            } else {
                AbstractNotebookVersionDTO parentDTO = versionMap.get(dto.getParentId());
                HistoryNode parentNode = nodeMap.get(dto.getParentId());
                if (parentNode == null) {
                    parentNode = HistoryNode.fromVersionDto(parentDTO, dateFormat);
                    nodeMap.put(parentNode.getId(), parentNode);
                }
                parentNode.getChildList().add(node);
            }
        }
    }

    public String toTreesString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name).append("\r\n");
        for (HistoryNode node : nodeList) {
            nodeToTreeString(node, stringBuilder, 1);
        }
        return stringBuilder.toString();
    }

    private void nodeToTreeString(HistoryNode node, StringBuilder stringBuilder, int level) {
        for (int i = 0; i < level; i ++) {
            stringBuilder.append("   ");
        }
        stringBuilder.append(node.getLabel());
        stringBuilder.append("\r\n");
        for (HistoryNode child : node.getChildList()) {
            nodeToTreeString(child, stringBuilder, level + 1);
        }
    }


}
