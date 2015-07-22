package portal.webapp;

import com.im.lac.job.jobdef.AsyncLocalProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.DatasetJobDefinition;
import com.im.lac.types.MoleculeObject;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class WorkflowPage extends WebPage {

    public static final String DROP_DATA_TYPE = "dropDataType";
    public static final String DROP_DATA_ID = "dropDataId";
    public static final String SOURCE_ID = "sourceId";
    public static final String TARGET_ID = "targetId";
    public static final String POSITION_X = "positionX";
    public static final String POSITION_Y = "positionY";
    public static final String CANVASITEM_INDEX = "index";
    public static final String CANVASITEM_WICKETID = "canvasItem";
    private static final Logger logger = LoggerFactory.getLogger(WorkflowPage.class);
    boolean datasetsVisibility = true;
    boolean servicesVisibility = true;
    boolean jobsVisibility = true;
    boolean canvasVisibility = true;
    boolean visualizersVisibility = true;
    private List<AbstractCanvasItemData> canvasItemDataList = new ArrayList<>();
    private ListView<AbstractCanvasItemData> canvasItemRepeater;
    private WebMarkupContainer plumbContainer;
    private AjaxLink canvasToggle;
    private JobsPanel jobsPanel;
    private AjaxLink jobsToggle;
    private VisualizersPanel visualizersPanel;
    private AjaxLink visualizersToggle;
    private AjaxLink datasetsToggle;
    private DatasetsPanel datasetsPanel;
    private AjaxLink servicesToggle;
    private ServicesPanel servicesPanel;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetsSession datasetsSession;
    @Inject
    private ServiceDiscoverySession serviceDiscoverySession;
    @Inject
    private JobsSession jobsSession;

    public WorkflowPage() {
        notifierProvider.createNotifier(this, "notifier");
        addPanels();
        addCanvas();
        addCanvasDropBehavior();
        addCanvasItemDragStopBehavior();
        addCanvasNewConnectionBehavior();
        addActions();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(WorkflowPage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(WorkflowPage.class, "resources/dom.jsPlumb-1.7.5.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(WorkflowPage.class, "resources/Canvas.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(WorkflowPage.class, "resources/lac.js")));
        response.render(OnDomReadyHeaderItem.forScript("init(); tabularMenu()"));
    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));

        datasetsPanel = new DatasetsPanel("datasets");
        add(datasetsPanel);
        datasetsPanel.setOutputMarkupPlaceholderTag(true);

        servicesPanel = new ServicesPanel("services");
        add(servicesPanel);
        servicesPanel.setOutputMarkupPlaceholderTag(true);

        jobsPanel = new JobsPanel("jobs");
        add(jobsPanel);
        jobsPanel.setOutputMarkupPlaceholderTag(true);

        visualizersPanel = new VisualizersPanel("visualizers");
        add(visualizersPanel);
        visualizersPanel.setOutputMarkupPlaceholderTag(true);
    }

    private void refreshPanelsVisibility(AjaxRequestTarget target) {
        target.appendJavaScript("applyWorkflowPageLayout('" + datasetsVisibility + "', '" + servicesVisibility + "', '" + canvasVisibility + "', '" + jobsVisibility + "', '" + visualizersVisibility + "')");
    }

    private void addActions() {
        datasetsToggle = new AjaxLink("datasetsToggle") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                datasetsVisibility = !datasetsVisibility;
                target.appendJavaScript("makeVerticalItemActive('" + datasetsToggle.getMarkupId() + "')");
                refreshPanelsVisibility(target);
            }
        };
        add(datasetsToggle);

        servicesToggle = new AjaxLink("servicesToggle") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                servicesVisibility = !servicesVisibility;
                target.appendJavaScript("makeVerticalItemActive('" + servicesToggle.getMarkupId() + "')");
                refreshPanelsVisibility(target);
            }
        };
        add(servicesToggle);

        canvasToggle = new AjaxLink("canvasToggle") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                canvasVisibility = !canvasVisibility;
                target.appendJavaScript("makeVerticalItemActive('" + canvasToggle.getMarkupId() + "')");
                refreshPanelsVisibility(target);
            }
        };
        add(canvasToggle);

        jobsToggle = new AjaxLink("jobsToggle") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                jobsVisibility = !jobsVisibility;
                target.appendJavaScript("makeVerticalItemActive('" + jobsToggle.getMarkupId() + "')");
                refreshPanelsVisibility(target);
            }
        };
        add(jobsToggle);

        visualizersToggle = new AjaxLink("visualizersToggle") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                visualizersVisibility = !visualizersVisibility;
                target.appendJavaScript("makeVerticalItemActive('" + visualizersToggle.getMarkupId() + "')");
                refreshPanelsVisibility(target);
            }
        };
        add(visualizersToggle);
    }

    private void addCanvas() {
        plumbContainer = new WebMarkupContainer("plumbContainer");
        plumbContainer.setOutputMarkupId(true);
        plumbContainer.setOutputMarkupPlaceholderTag(true);
        add(plumbContainer);

        canvasItemRepeater = new ListView<AbstractCanvasItemData>(CANVASITEM_WICKETID, canvasItemDataList) {

            @Override
            protected void populateItem(ListItem<AbstractCanvasItemData> components) {
                components.setOutputMarkupId(true);
                AbstractCanvasItemData model = components.getModelObject();
                components.add(new AttributeModifier("style", "top:" + model.getPositionY() + "px; left:" + model.getPositionX() + "px;"));
                if (model instanceof DatasetCanvasItemData) {
                    DatasetCanvasItemData datasetCanvasItemData = (DatasetCanvasItemData) model;
                    components.add(new DatasetCanvasItemPanel("item", datasetCanvasItemData));
                }
            }
        };
        canvasItemRepeater.setOutputMarkupId(true);
        plumbContainer.add(canvasItemRepeater);
    }

    private void addCanvasItem(AjaxRequestTarget target) {
        String dropDataType = getRequest().getRequestParameters().getParameterValue(DROP_DATA_TYPE).toString();
        String dropDataId = getRequest().getRequestParameters().getParameterValue(DROP_DATA_ID).toString();
        String x = getRequest().getRequestParameters().getParameterValue(POSITION_X).toString();
        String y = getRequest().getRequestParameters().getParameterValue(POSITION_Y).toString();

        logger.debug("Drop data " + dropDataId + " at " + POSITION_X + ": " + x + " " + POSITION_Y + ": " + y);

        AbstractCanvasItemData data = null;
        Panel canvasItemPanel = null;

        if (ServicesPanel.DROP_DATA_TYPE_VALUE.equals(dropDataType)) {
            ServiceCanvasItemData serviceCanvasItemData = new ServiceCanvasItemData();
            serviceCanvasItemData.setServiceDescriptor(serviceDiscoverySession.findServiceDescriptorById(Long.parseLong(dropDataId)));
            serviceCanvasItemData.setPositionX(x);
            serviceCanvasItemData.setPositionY(y);
            ServiceCanvasItemPanel serviceCanvasItemPanel = createServiceCanvasItemPanel(serviceCanvasItemData);
            canvasItemDataList.add(serviceCanvasItemData);
            data = serviceCanvasItemData;
            canvasItemPanel = serviceCanvasItemPanel;
        } else if (LegacyDatasetsPanel.DROP_DATA_TYPE_VALUE.equals(dropDataType)) {
            DatasetCanvasItemData datasetCanvasItemData = new DatasetCanvasItemData();
            datasetCanvasItemData.setDatasetDescriptor(datasetsSession.findDatasetDescriptorById(Long.parseLong(dropDataId)));
            datasetCanvasItemData.setPositionX(x);
            datasetCanvasItemData.setPositionY(y);
            DatasetCanvasItemPanel datasetCanvasItemPanel = createDatasetCanvasItemPanel(datasetCanvasItemData);
            canvasItemDataList.add(datasetCanvasItemData);
            data = datasetCanvasItemData;
            canvasItemPanel = datasetCanvasItemPanel;
        }

        if (data != null) {
            ListItem listItem = new ListItem(dropDataType + dropDataId, canvasItemDataList.size());
            listItem.setOutputMarkupId(true);
            listItem.add(new AttributeModifier("style", "top:" + data.getPositionY() + "px; left:" + data.getPositionX() + "px;"));
            listItem.add(canvasItemPanel);
            canvasItemRepeater.add(listItem);

            String script = "addCanvasItem(':plumbContainerId', ':itemId')";
            target.prependJavaScript(script
                    .replaceAll(":plumbContainerId", plumbContainer.getMarkupId())
                    .replaceAll(":itemId", listItem.getMarkupId()));

            target.add(listItem);

            target.appendJavaScript("makeCanvasItemsDraggable(':itemId')".replaceAll(":itemId", "#" + listItem.getMarkupId()));
            target.appendJavaScript("addSourceEndpoint(':itemId')".replaceAll(":itemId", listItem.getMarkupId()));
            target.appendJavaScript("addTargetEndpoint(':itemId')".replaceAll(":itemId", listItem.getMarkupId()));
        }
    }

    private DatasetCanvasItemPanel createDatasetCanvasItemPanel(DatasetCanvasItemData datasetCanvasItemData) {
        return new DatasetCanvasItemPanel("item", datasetCanvasItemData);
    }

    private ServiceCanvasItemPanel createServiceCanvasItemPanel(final ServiceCanvasItemData serviceCanvasItemData) {
        return new ServiceCanvasItemPanel("item", serviceCanvasItemData, new ServiceCanvasItemPanel.Callbacks() {

            @Override
            public void onServiceCanvasItemDelete() {
                removeCanvasItem(serviceCanvasItemData);
            }

            @Override
            public void onServiceCanvasItemSave() {
            }
        });
    }

    private void removeCanvasItem(AbstractCanvasItemData abstractCanvasItemData) {
        int indexToRemove = -1;
        for (int i = 0; i < canvasItemDataList.size(); i++) {
            AbstractCanvasItemData data = canvasItemDataList.get(i);
            if (data.equals(abstractCanvasItemData)) {
                indexToRemove = i;
            }
        }
        if (indexToRemove != -1) {
            Component listItemToRemove = canvasItemRepeater.get(indexToRemove);
            canvasItemRepeater.remove(listItemToRemove);
            canvasItemDataList.remove(indexToRemove);
            AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
            target.appendJavaScript("removeCanvasItem(':itemId')".replaceAll(":itemId", listItemToRemove.getMarkupId()));
        }
    }

    private void addCanvasDropBehavior() {
        AbstractDefaultAjaxBehavior onCanvasDropBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                addCanvasItem(target);
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                CharSequence callBackScript = getCallbackFunction(
                        CallbackParameter.explicit(DROP_DATA_TYPE),
                        CallbackParameter.explicit(DROP_DATA_ID),
                        CallbackParameter.explicit(POSITION_X),
                        CallbackParameter.explicit(POSITION_Y));
                callBackScript = "onCanvasDrop=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onCanvasDropBehavior);
    }

    private void addCanvasItemDragStopBehavior() {
        AbstractDefaultAjaxBehavior onCanvasItemDragStopBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                String index = getRequest().getRequestParameters().getParameterValue(CANVASITEM_INDEX).toString();
                String x = getRequest().getRequestParameters().getParameterValue(POSITION_X).toString();
                String y = getRequest().getRequestParameters().getParameterValue(POSITION_Y).toString();

                logger.info("Item index " + index + " Dragged to: " + POSITION_X + ": " + x + " " + POSITION_Y + ": " + y);

                int i = Integer.parseInt(index);
                AbstractCanvasItemData model = canvasItemDataList.get(i);
                model.setPositionX(x);
                model.setPositionY(y);
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                CharSequence callBackScript = getCallbackFunction(
                        CallbackParameter.explicit(CANVASITEM_INDEX),
                        CallbackParameter.explicit(POSITION_X),
                        CallbackParameter.explicit(POSITION_Y));
                callBackScript = "onCanvasItemDragStop=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onCanvasItemDragStopBehavior);
    }

    private void addCanvasNewConnectionBehavior() {
        AbstractDefaultAjaxBehavior onCanvasNewConnectionBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                String sourceId = getRequest().getRequestParameters().getParameterValue(SOURCE_ID).toString();
                String targetId = getRequest().getRequestParameters().getParameterValue(TARGET_ID).toString();

                DatasetCanvasItemData sourceData = null;
                ServiceCanvasItemData targetData = null;
                for (Component li : canvasItemRepeater) {
                    ListItem item = (ListItem) li;
                    Component panel = item.get(0);

                    if (item.getMarkupId().equals(sourceId)) {
                        if (panel instanceof DatasetCanvasItemPanel) {
                            DatasetCanvasItemPanel datasetCanvasItemPanel = (DatasetCanvasItemPanel) panel;
                            sourceData = datasetCanvasItemPanel.getData();
                        }
                    }

                    if (item.getMarkupId().equals(targetId)) {
                        if (panel instanceof ServiceCanvasItemPanel) {
                            ServiceCanvasItemPanel datasetCanvasItemPanel = (ServiceCanvasItemPanel) panel;
                            targetData = datasetCanvasItemPanel.getData();
                        }
                    }
                }

                if (sourceData != null && targetData != null) {

                    logger.info("New connection: " + sourceData.getDatasetDescriptor().getDescription() + " --> " + targetData.getServiceDescriptor().getName());

                    postSimpleJob(sourceData, targetData);
                }
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                CharSequence callBackScript = getCallbackFunction(
                        CallbackParameter.explicit(SOURCE_ID),
                        CallbackParameter.explicit(TARGET_ID));
                callBackScript = "onCanvasNewConnection=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onCanvasNewConnectionBehavior);
    }

    private void postSimpleJob(DatasetCanvasItemData sourceData, ServiceCanvasItemData targetData) {
        String endpoint = targetData.getServiceDescriptor().getEndpoint();
        if ("direct:simpleroute".equals(endpoint)) {
            AsyncLocalProcessDatasetJobDefinition jobDefinition = new AsyncLocalProcessDatasetJobDefinition(
                    sourceData.getDatasetDescriptor().getId(),
                    endpoint,
                    DatasetJobDefinition.DatasetMode.CREATE,
                    MoleculeObject.class,
                    "Gustavo 1");
            jobsSession.submitJob(jobDefinition);
        }
    }
}
