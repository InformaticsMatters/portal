package portal.webapp;

import com.vaynberg.wicket.select2.Select2Choice;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import portal.integration.DatamartSession;
import portal.integration.PropertyData;
import portal.integration.PropertyDefinition;
import portal.service.api.DatasetDescriptor;
import portal.service.api.DatasetService;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import javax.swing.tree.TreeModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreeGridVisualizerPage extends WebPage {

    private final DatasetDescriptor datasetDescriptor;

    private Form<PropertyDefinitionData> form;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService service;
    @Inject
    private DatamartSession datamartSession;
    private TreeGridVisualizer treeGridVisualizer;
    private ListView<String> columnsListView;
    private WebMarkupContainer columnsContainer;
    private MenuPanel menuPanel;

    public TreeGridVisualizerPage(DatasetDescriptor datasetDescriptor) {
        this.datasetDescriptor = datasetDescriptor;
        notifierProvider.createNotifier(this, "notifier");
        addMenuPanel();
        addOrReplaceTreeGridVisualizer(datasetDescriptor);
        addColumnsForm();
        addColumnsListView();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/lac.js")));
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
    }

    private void addMenuPanel() {
        menuPanel = new MenuPanel("menuPanel");
        menuPanel.setLeftSideItemVisible(true);
        add(menuPanel);
    }

    private void addOrReplaceTreeGridVisualizer(DatasetDescriptor datasetDescriptor) {
        treeGridVisualizer = new TreeGridVisualizer("treeGrid", datasetDescriptor);
        addOrReplace(treeGridVisualizer);
        TreeGridNavigationPanel treeGridNavigation = new TreeGridNavigationPanel("treeGridNavigation", treeGridVisualizer);
        addOrReplace(treeGridNavigation);
    }

    private void addColumnsForm() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new PropertyDefinitionData()));
        form.setOutputMarkupId(true);
        add(form);

        // why not just to inject the provider dependant scope?
        PropertyDefinitionProvider propertyDefinitionProvider = new PropertyDefinitionProvider();
        CdiContainer.get().getNonContextualManager().postConstruct(propertyDefinitionProvider);

        Select2Choice<PropertyDefinition> definitionChoice = new Select2Choice<>("propertyDefinition");
        definitionChoice.setProvider(propertyDefinitionProvider);
        definitionChoice.getSettings().setMinimumInputLength(4);
        definitionChoice.setOutputMarkupId(true);
        form.add(definitionChoice);

        definitionChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                onPropertyDefinitionSelected();
            }
        });
    }

    private void addColumnsListView() {
        columnsContainer = new WebMarkupContainer("columnsContainer");
        columnsContainer.setOutputMarkupId(true);
        columnsListView = new ListView<String>("columnList") {

            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("label", item.getModel()));
                item.add(new AjaxLink("add") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        datamartSession.addPropertyToDataset(datasetDescriptor, item.getModelObject());
                        addOrReplaceTreeGridVisualizer(datasetDescriptor);
                        ajaxRequestTarget.add(treeGridVisualizer);

                        TreeModel treeModel = treeGridVisualizer.getTree().getModelObject();


                    }
                });
            }
        };
        columnsContainer.add(columnsListView);
        add(columnsContainer);
    }

    private void onPropertyDefinitionSelected() {
        ArrayList<String> columnList = new ArrayList<>();
        PropertyDefinition propertyDefinition = form.getModelObject().getPropertyDefinition();
        if (propertyDefinition != null) {
            List<PropertyData> propertyDataList = datamartSession.listPropertyData(datasetDescriptor, propertyDefinition);
            if (propertyDataList != null && propertyDataList.size() > 0) {
                PropertyData propertyData = propertyDataList.get(0);
                Set<String> keyset = propertyData.getData().keySet();
                columnList.addAll(keyset);
            } else {
                System.out.println("No data for that property");
            }
        }
        columnsListView.setList(columnList);
        getRequestCycle().find(AjaxRequestTarget.class).add(columnsContainer);
    }

    private class PropertyDefinitionData implements Serializable {

        private PropertyDefinition propertyDefinition;

        public PropertyDefinition getPropertyDefinition() {
            return propertyDefinition;
        }

        public void setPropertyDefinition(PropertyDefinition propertyDefinition) {
            this.propertyDefinition = propertyDefinition;
        }
    }

    private class ColumnData implements Serializable {

        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
