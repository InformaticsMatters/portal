package portal.webapp.visualizers;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import portal.dataset.IDatasetDescriptor;
import portal.webapp.FooterPanel;
import portal.webapp.MenuPanel;
import portal.webapp.PortalWebApplication;
import portal.webapp.workflow.DatasetsSession;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class TreeGridVisualizerPage extends WebPage {

    private final IDatasetDescriptor datasetDescriptor;
    private TreeGridVisualizer treeGridVisualizer;
    private MenuPanel menuPanel;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetsSession datasetsSession;

    public TreeGridVisualizerPage(IDatasetDescriptor datasetDescriptor) {
        this.datasetDescriptor = datasetDescriptor;
        notifierProvider.createNotifier(this, "notifier");
        addFooter();
        addOrReplaceTreeGridVisualizer(datasetDescriptor);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/lac.js")));
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/lac.css")));
    }

    private void addFooter() {
        add(new FooterPanel("footerPanel"));
    }

    private void addOrReplaceTreeGridVisualizer(IDatasetDescriptor datasetDescriptor) {
        treeGridVisualizer = new TreeGridVisualizer("treeGrid", datasetDescriptor);
        addOrReplace(treeGridVisualizer);
        TreeGridNavigationPanel treeGridNavigation = new TreeGridNavigationPanel("treeGridNavigation", treeGridVisualizer);
        addOrReplace(treeGridNavigation);
    }
}
