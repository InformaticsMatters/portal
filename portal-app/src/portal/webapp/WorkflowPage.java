package portal.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
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
import portal.integration.DatamartSession;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class WorkflowPage extends WebPage {

    public static final String DROP_DATA_TYPE = "dropDataType";
    public static final String DROP_DATA_ID = "dropDataId";
    public static final String POSITION_X = "positionX";
    public static final String POSITION_Y = "positionY";
    public static final String CANVASITEM_INDEX = "index";
    public static final String CANVASITEM_WICKETID = "canvasItem";

    private List<AbstractCanvasItemData> canvasItemModelList = new ArrayList<>();
    private ListView<AbstractCanvasItemData> canvasItemRepeater;
    private WebMarkupContainer plumbContainer;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatamartSession datamartSession;
    @Inject
    private ServiceDiscoverySession serviceDiscoverySession;

    public WorkflowPage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addDatasetsPanel();
        addServicesPanel();
        addCanvas();
        addCanvasDropBehavior();
        addCanvasItemDragStopBehavior();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(WorkflowPage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(WorkflowPage.class, "resources/dom.jsPlumb-1.6.2.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(WorkflowPage.class, "resources/Canvas.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(WorkflowPage.class, "resources/lac.js")));
        response.render(OnDomReadyHeaderItem.forScript("init(); tabularMenu()"));
    }

    private void addDatasetsPanel() {
        add(new DatasetsPanel("datasets"));
    }

    private void addServicesPanel() {
        add(new ServicesPanel("services"));
    }

    private void addCanvas() {
        plumbContainer = new WebMarkupContainer("plumbContainer");
        plumbContainer.setOutputMarkupId(true);
        add(plumbContainer);

        canvasItemRepeater = new ListView<AbstractCanvasItemData>(CANVASITEM_WICKETID, canvasItemModelList) {

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
        plumbContainer.add(canvasItemRepeater);
    }

    private void addCanvasItem(AjaxRequestTarget target) {
        String dropDataType = getRequest().getRequestParameters().getParameterValue(DROP_DATA_TYPE).toString();
        String dropDataId = getRequest().getRequestParameters().getParameterValue(DROP_DATA_ID).toString();
        String x = getRequest().getRequestParameters().getParameterValue(POSITION_X).toString();
        String y = getRequest().getRequestParameters().getParameterValue(POSITION_Y).toString();
        System.out.println("Drop data " + dropDataId + " at " + POSITION_X + ": " + x + " " + POSITION_Y + ": " + y);

        AbstractCanvasItemData data = null;
        Panel canvasItemPanel = null;

        if (ServicesPanel.DROP_DATA_TYPE_VALUE.equals(dropDataType)) {
            ServiceCanvasItemData serviceCanvasItemData = new ServiceCanvasItemData();
            serviceCanvasItemData.setServiceDescriptor(serviceDiscoverySession.findServiceDescriptorById(Long.parseLong(dropDataId)));
            serviceCanvasItemData.setPositionX(x);
            serviceCanvasItemData.setPositionY(y);
            ServiceCanvasItemPanel serviceCanvasItemPanel = new ServiceCanvasItemPanel("item", serviceCanvasItemData);
            canvasItemModelList.add(serviceCanvasItemData);
            data = serviceCanvasItemData;
            canvasItemPanel = serviceCanvasItemPanel;
        } else if (DatasetsPanel.DROP_DATA_TYPE_VALUE.equals(dropDataType)) {
            DatasetCanvasItemData datasetCanvasItemData = new DatasetCanvasItemData();
            datasetCanvasItemData.setDatasetDescriptor(datamartSession.findDatasetDescriptorById(Long.parseLong(dropDataId)));
            datasetCanvasItemData.setPositionX(x);
            datasetCanvasItemData.setPositionY(y);
            DatasetCanvasItemPanel datasetCanvasItemPanel = new DatasetCanvasItemPanel("item", datasetCanvasItemData);
            canvasItemModelList.add(datasetCanvasItemData);
            data = datasetCanvasItemData;
            canvasItemPanel = datasetCanvasItemPanel;
        }

        if (data != null) {
            ListItem listItem = new ListItem(dropDataType + dropDataId, canvasItemModelList.size());
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
                System.out.println("Item index " + index + " Dragged to: " + POSITION_X + ": " + x + " " + POSITION_Y + ": " + y);

                int i = Integer.parseInt(index);
                AbstractCanvasItemData model = canvasItemModelList.get(i);
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
}
