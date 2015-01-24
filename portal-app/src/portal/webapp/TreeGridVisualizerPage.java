package portal.webapp;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.common.AbstractGrid;
import com.vaynberg.wicket.select2.ApplicationSettings;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import portal.service.api.*;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class TreeGridVisualizerPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService service;

    public TreeGridVisualizerPage(DatasetDescriptor datasetDescriptor) {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addTreeGrid(datasetDescriptor);
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

    public void addTreeGrid(DatasetDescriptor datasetDescriptor) {
        ListRowFilter listRowFilter = new ListRowFilter();
        listRowFilter.setDatasetId(datasetDescriptor.getId());
        List<Row> rowList = service.listRow(listRowFilter);

        TreeGridVisualizerNode rootNode = new TreeGridVisualizerNode();
        buildNodeHierarchy(rootNode, rowList);

        List<IGridColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String>> columns = new ArrayList<IGridColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String>>();
        TreeGridVisualizerTreeColumn treeColumn = new TreeGridVisualizerTreeColumn("id", Model.of("Structure"), datasetDescriptor);
        columns.add(treeColumn);
        for (RowDescriptor rowDescriptor : datasetDescriptor.listAllRowDescriptors()) {
            for (PropertyDescriptor propertyDescriptor : rowDescriptor.listAllPropertyDescriptors()) {
                columns.add(new TreeGridVisualizerPropertyColumn(propertyDescriptor.getId().toString(), Model.of(propertyDescriptor.getDescription()), propertyDescriptor.getId()));
            }
        }

        TreeGridVisualizer treeGridVisualizer = new TreeGridVisualizer("treeGrid", new TreeGridVisualizerModel(rootNode), columns);
        treeGridVisualizer.getTree().setRootLess(true);
        add(treeGridVisualizer);
    }

    private void buildNodeHierarchy(TreeGridVisualizerNode rootNode, List<Row> rowList) {
        for (Row row : rowList) {
            TreeGridVisualizerNode childNode = new TreeGridVisualizerNode();
            childNode.setUserObject(row);
            rootNode.add(childNode);
            if (row.getChildren() != null && row.getChildren().size() > 0) {
                buildNodeHierarchy(childNode, row.getChildren());
            }
        }
    }
}
