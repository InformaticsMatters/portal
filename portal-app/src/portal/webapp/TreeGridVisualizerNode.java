package portal.webapp;

import portal.service.api.Row;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeGridVisualizerNode extends DefaultMutableTreeNode {

    @Override
    public Row getUserObject() {
        return (Row) super.getUserObject();
    }
}
