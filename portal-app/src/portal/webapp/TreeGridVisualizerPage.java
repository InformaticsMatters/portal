package portal.webapp;

import com.inmethod.grid.common.AbstractGrid;
import com.vaynberg.wicket.select2.ApplicationSettings;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.request.resource.CssResourceReference;
import portal.service.api.DatasetDescriptor;
import portal.service.api.DatasetService;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class TreeGridVisualizerPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService service;

    public TreeGridVisualizerPage(DatasetDescriptor datasetDescriptor) {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addPageableTreeGrid(datasetDescriptor);
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(AbstractGrid.class, "res/style.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(ApplicationSettings.class, "res/select2.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/semantic-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/easygrid-overrides.css")));
    }

    private void addPageableTreeGrid(DatasetDescriptor datasetDescriptor) {
        TreeGridVisualizer treeGridVisualizer = new TreeGridVisualizer("treeGrid", datasetDescriptor);
        treeGridVisualizer.getTree().setRootLess(true);
        add(treeGridVisualizer);

        AjaxPagingNavigation navigation = new AjaxPagingNavigation("navigation", treeGridVisualizer);
        add(navigation);
    }

}
