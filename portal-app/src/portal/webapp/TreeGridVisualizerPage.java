package portal.webapp;

import com.vaynberg.wicket.select2.Select2Choice;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import portal.integration.PropertyDefinition;
import portal.service.api.DatasetDescriptor;
import portal.service.api.DatasetService;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class TreeGridVisualizerPage extends WebPage {

    private AjaxLink leftSidebar;
    private AjaxLink rightSidebar;

    private Form<ColumnsData> form;

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
        addActions();
        addForm();
        addColumnsListView();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/lac.js")));
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
    }

    private void addPageableTreeGrid(DatasetDescriptor datasetDescriptor) {
        treeGridVisualizer = new TreeGridVisualizer("treeGrid", datasetDescriptor);
        add(treeGridVisualizer);

        add(new TreeGridNavigationPanel("treeGridNavigation", treeGridVisualizer));
    }

    private void addActions() {
        leftSidebar = new AjaxLink("leftSidebar") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                ajaxRequestTarget.appendJavaScript("leftSideBarToggle()");
            }
        };
        add(leftSidebar);

        rightSidebar = new AjaxLink("rightSidebar") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                ajaxRequestTarget.appendJavaScript("rightSideBarToggle()");
            }
        };
        add(rightSidebar);
    }

    private void addForm() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new ColumnsData()));
        form.setOutputMarkupId(true);
        add(form);

        PropertyDefinitionProvider propertyDefinitionProvider = new PropertyDefinitionProvider();
        CdiContainer.get().getNonContextualManager().postConstruct(propertyDefinitionProvider);

        Select2Choice<PropertyDefinition> propertyDefinition = new Select2Choice<>("propertyDefinition");
        propertyDefinition.setProvider(propertyDefinitionProvider);
        propertyDefinition.getSettings().setMinimumInputLength(4);
        propertyDefinition.setOutputMarkupId(true);
        form.add(propertyDefinition);
    }

    private void addColumnsListView() {
        List list = Arrays.asList(new String[]{"Column 1", "Column 2", "Column 3"});
        ListView listItem = new ListView("listItem", list) {
            protected void populateItem(ListItem item) {
                item.add(new Label("label", item.getModel()));
            }
        };
        add(listItem);
    }

    private class ColumnsData implements Serializable {

        private PropertyDefinition propertyDefinition;


        public PropertyDefinition getPropertyDefinition() {
            return propertyDefinition;
        }

        public void setPropertyDefinition(PropertyDefinition propertyDefinition) {
            this.propertyDefinition = propertyDefinition;
        }
    }
}
