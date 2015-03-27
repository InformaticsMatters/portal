package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import portal.service.api.DatasetDescriptor;
import portal.service.api.DatasetService;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TreeGridVisualizerPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService service;
    private AjaxPagingNavigation navigation;
    private TreeGridVisualizer treeGridVisualizer;

    public TreeGridVisualizerPage(DatasetDescriptor datasetDescriptor) {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addPageableTreeGrid(datasetDescriptor);
        add(new AjaxLink("expand") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                DefaultTreeModel defaultModelObject = (DefaultTreeModel) treeGridVisualizer.getDefaultModelObject();
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) defaultModelObject.getRoot();
                treeGridVisualizer.getTreeState().expandNode(root.getFirstChild());
                treeGridVisualizer.getTree().invalidateAll();
                treeGridVisualizer.getTree().updateTree(ajaxRequestTarget);
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
    }

    private void addPageableTreeGrid(DatasetDescriptor datasetDescriptor) {
        treeGridVisualizer = new TreeGridVisualizer("treeGrid", datasetDescriptor);
        add(treeGridVisualizer);

        add(new TreeGridNavigationPanel("treeGridNavigation", treeGridVisualizer));
    }
}
