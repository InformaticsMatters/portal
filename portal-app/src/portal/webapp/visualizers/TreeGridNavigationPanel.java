package portal.webapp.visualizers;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class TreeGridNavigationPanel extends Panel {

    private final TreeGridVisualizer treeGridVisualizer;

    public TreeGridNavigationPanel(String id, TreeGridVisualizer treeGridVisualizer) {
        super(id);
        setOutputMarkupId(true);
        this.treeGridVisualizer = treeGridVisualizer;
        addNavigationControls();
    }

    private void addNavigationControls() {
        add(new AjaxLink("first") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                treeGridVisualizer.setCurrentPage(0);
            }
        });

        add(new AjaxLink("last") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                treeGridVisualizer.setCurrentPage(treeGridVisualizer.getPageCount() - 1);
            }
        });

        add(new AjaxPagingNavigation("navigation", treeGridVisualizer) {

            @Override
            public void onEvent(IEvent<?> event) {
                super.onEvent(event);
                AjaxRequestTarget ajaxRequestTarget = getRequestCycle().find(AjaxRequestTarget.class);
                ajaxRequestTarget.add(TreeGridNavigationPanel.this);
                ajaxRequestTarget.add(treeGridVisualizer);
            }
        });
    }
}

