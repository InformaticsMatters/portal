package portal.webapp;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;

/**
 * @author simetrias
 */
public class TreeGridVisualizerPaginator extends AjaxPagingNavigation {

    private final TreeGridVisualizer treeGridVisualizer;

    public TreeGridVisualizerPaginator(String id, TreeGridVisualizer treeGridVisualizer) {
        super(id, treeGridVisualizer);
        this.treeGridVisualizer = treeGridVisualizer;
    }


}
