package portal.notebook;

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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.notebook.api.CellType;
import portal.FooterPanel;
import portal.MenuPanel;
import portal.PortalHomePage;
import portal.notebook.service.NotebookInfo;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookCanvasPage extends WebPage {

    public static final String DROP_DATA_TYPE = "dropDataType";
    public static final String DROP_DATA_ID = "dropDataId";
    public static final String SOURCE_ID = "sourceId";
    public static final String TARGET_ID = "targetId";
    public static final String POSITION_LEFT = "positionX";
    public static final String POSITION_TOP = "positionY";
    public static final String CANVAS_ITEM_PREFIX = "canvasItem";
    public static final String CANVASITEM_INDEX = "index";
    private static final Logger logger = LoggerFactory.getLogger(NotebookCanvasPage.class);

    boolean nbListVisible = true;
    boolean cellsVisible = true;
    boolean canvasVisible = true;
    private AjaxLink nbListToggle;
    private AjaxLink cellsToggle;
    private AjaxLink canvasToggle;

    private NotebookListPanel notebookListPanel;
    private NotebookCellTypesPanel notebookCellTypesPanel;
    private WebMarkupContainer plumbContainer;
    private ConnectionPanel connectionPanel;

    private ListView<CellModel> canvasItemRepeater;

    private CellTitleBarPanel.CallbackHandler cellCallbackHandler;
    private EditNotebookPanel editNotebookPanel;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private NotebookSession notebookSession;

    public NotebookCanvasPage() {
        notifierProvider.createNotifier(this, "notifier");
        setOutputMarkupId(true);
        addPanels();
        addActions();
        addCanvasPaletteDropBehavior();
        addCanvasItemDraggedBehavior();
        addCanvasNewConnectionBehavior();
        addConnectionsRenderBehavior();
        NotebookInfo notebookInfo = notebookSession.preparePocNotebook();
        notebookSession.loadCurrentNotebook(notebookInfo.getId());
        addListeners();
    }

    private void addListeners() {
        cellCallbackHandler = new CellTitleBarPanel.CallbackHandler() {

            @Override
            public void onRemove(CellModel cellModel) {
                notebookSession.removeCell(cellModel);
                RequestCycle.get().find(AjaxRequestTarget.class).add(NotebookCanvasPage.this);
                // RequestCycle.get().find(AjaxRequestTarget.class).appendJavaScript("addCellsPaletteDragAndDropSupport();");
                //RequestCycle.get().find(AjaxRequestTarget.class).appendJavaScript("makeCanvasItemPlumbDraggable('.notebook-canvas-item');");
                //RequestCycle.get().find(AjaxRequestTarget.class).appendJavaScript(buildConnectionsJS());
            }

            @Override
            public void onEditBindings(CellModel cellModel) {
                connectionPanel.configure(null, cellModel);
                connectionPanel.setCanAddBindings(false);
                connectionPanel.showModal();
            }

            @Override
            public void onContentChanged() {
                notebookSession.reloadCurrentNotebook();
                RequestCycle.get().find(AjaxRequestTarget.class).add(NotebookCanvasPage.this);
            }
        };
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/jquery-ui-simetrias.min.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/jquery-ui.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/dom.jsPlumb-1.7.5.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/lac.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/notebook.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/notebook.js")));
        response.render(OnDomReadyHeaderItem.forScript("initJsPlumb(); addCellsPaletteDragAndDropSupport();"));
        response.render(OnDomReadyHeaderItem.forScript("makeCanvasItemPlumbDraggable('.notebook-canvas-item'); makeCanvasItemPlumbDraggable();"));

    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));

        notebookCellTypesPanel = new NotebookCellTypesPanel("descriptors");
        add(notebookCellTypesPanel);
        notebookCellTypesPanel.setOutputMarkupPlaceholderTag(true);

        add(new Label("notebookName", new IModel<String>() {
            @Override
            public void detach() {

            }

            @Override
            public String getObject() {
                return notebookSession.getCurrentNotebookInfo() == null ? "" : notebookSession.getCurrentNotebookInfo().getName();
            }

            @Override
            public void setObject(String s) {

            }
        }));

        plumbContainer = new WebMarkupContainer("plumbContainer");
        plumbContainer.setOutputMarkupId(true);
        plumbContainer.setOutputMarkupPlaceholderTag(true);
        add(plumbContainer);

        IModel<List<CellModel>> listModel = new IModel<List<CellModel>>() {

            @Override
            public List<CellModel> getObject() {
                if (notebookSession.getCurrentNotebookModel() == null) {
                    return new ArrayList<>();
                } else {
                    return Arrays.asList(notebookSession.getCurrentNotebookModel().getCellModels());
                }
            }

            @Override
            public void setObject(List<CellModel> cells) {

            }

            @Override
            public void detach() {

            }
        };
        canvasItemRepeater = new ListView<CellModel>("canvasItem", listModel) {

            @Override
            protected void populateItem(ListItem<CellModel> listItem) {
                CellModel cellModel = listItem.getModelObject();
                String markupId = CANVAS_ITEM_PREFIX + cellModel.getId();
                Panel canvasItemPanel = createCanvasItemPanel(cellModel);
                listItem.setOutputMarkupId(true);
                listItem.setMarkupId(markupId);
                listItem.add(new AttributeModifier("style", "left:" + cellModel.getPositionLeft() + "px; top:" + cellModel.getPositionTop() + "px;"));
                listItem.add(canvasItemPanel);
            }
        };
        canvasItemRepeater.setOutputMarkupId(true);
        plumbContainer.add(canvasItemRepeater);

        connectionPanel = new ConnectionPanel("connectionPanel", "modalElement");
        connectionPanel.setCallbacks(new ConnectionPanel.Callbacks() {

            @Override
            public void onSubmit() {
                notebookSession.storeCurrentNotebook();
                if (connectionPanel.getSourceCellModel() != null) {
                    AjaxRequestTarget ajaxRequestTarget = getRequestCycle().find(AjaxRequestTarget.class);
                    ajaxRequestTarget.add(NotebookCanvasPage.this);
                    String sourceMarkupId = CANVAS_ITEM_PREFIX + connectionPanel.getSourceCellModel().getId();
                    String targetMarkupId = CANVAS_ITEM_PREFIX + connectionPanel.getTargetCellModel().getId();
                    String js = "addConnection('" + sourceMarkupId + "', '" + targetMarkupId + "');";
                    ajaxRequestTarget.appendJavaScript(js);
                }
            }

            @Override
            public void onClose() {
                if (connectionPanel.isDirty()) {
                    notebookSession.reloadCurrentNotebook();
                    RequestCycle.get().find(AjaxRequestTarget.class).add(NotebookCanvasPage.this);
                }
            }
        });
        plumbContainer.add(connectionPanel);
        editNotebookPanel = new EditNotebookPanel("editNotebookPanel", "modalElement");
        add(editNotebookPanel);
        editNotebookPanel.setCallbacks(new EditNotebookPanel.Callbacks() {

            @Override
            public void onSubmit() {
                notebookListPanel.refreshNotebookList();
                AjaxRequestTarget ajaxRequestTarget = getRequestCycle().find(AjaxRequestTarget.class);
                ajaxRequestTarget.add(NotebookCanvasPage.this);
            }

            @Override
            public void onCancel() {

            }
        });
        notebookListPanel = new NotebookListPanel("notebookList", editNotebookPanel);
        add(notebookListPanel);
        notebookListPanel.setOutputMarkupPlaceholderTag(true);
    }

    private void refreshPanelsVisibility(AjaxRequestTarget target) {
        target.appendJavaScript("applyNotebookCanvasPageLayout('" + cellsVisible + "', '" + canvasVisible + "', '" + nbListVisible + "')");
    }

    private void addActions() {
        add(new AjaxLink("createNotebook") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                editNotebookPanel.configureForCreate();
                editNotebookPanel.showModal();
            }
        });

        nbListToggle = new AjaxLink("nbListToggle") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                nbListVisible = !nbListVisible;
                target.appendJavaScript("makeVerticalItemActive('" + nbListToggle.getMarkupId() + "')");
                refreshPanelsVisibility(target);
            }
        };
        add(nbListToggle);

        cellsToggle = new AjaxLink("cellsToggle") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                cellsVisible = !cellsVisible;
                target.appendJavaScript("makeVerticalItemActive('" + cellsToggle.getMarkupId() + "')");
                refreshPanelsVisibility(target);
            }
        };
        add(cellsToggle);

        canvasToggle = new AjaxLink("canvasToggle") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                canvasVisible = !canvasVisible;
                target.appendJavaScript("makeVerticalItemActive('" + canvasToggle.getMarkupId() + "')");
                refreshPanelsVisibility(target);
            }
        };
        add(canvasToggle);
    }

    private void addCanvasPaletteDropBehavior() {
        AbstractDefaultAjaxBehavior onCanvasDropBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                addCanvasItemFromDrop(target);
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                CharSequence callBackScript = getCallbackFunction(
                        CallbackParameter.explicit(DROP_DATA_TYPE),
                        CallbackParameter.explicit(DROP_DATA_ID),
                        CallbackParameter.explicit(POSITION_LEFT),
                        CallbackParameter.explicit(POSITION_TOP));
                callBackScript = "onNotebookCanvasPaletteDrop=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onCanvasDropBehavior);
    }

    private void addCanvasItemFromDrop(AjaxRequestTarget target) {
        String dropDataType = getRequest().getRequestParameters().getParameterValue(DROP_DATA_TYPE).toString();
        String dropDataId = getRequest().getRequestParameters().getParameterValue(DROP_DATA_ID).toString();
        String x = getRequest().getRequestParameters().getParameterValue(POSITION_LEFT).toString();
        String y = getRequest().getRequestParameters().getParameterValue(POSITION_TOP).toString();

        logger.info("Type: " + dropDataType + " ID: " + dropDataId + " at " + POSITION_LEFT + ": " + x + " " + POSITION_TOP + ": " + y);

        CellType cellType = notebookSession.findCellType(dropDataId);
        CellModel cellModel = notebookSession.addCell(cellType, Integer.parseInt(x), Integer.parseInt(y));

        NotebookModel notebookModel = notebookSession.getCurrentNotebookModel();

        Panel canvasItemPanel = createCanvasItemPanel(cellModel);

        List<CellModel> cellModelList = Arrays.asList(notebookModel.getCellModels());
        String markupId = CANVAS_ITEM_PREFIX + cellModel.getId();
        ListItem<CellModel> listItem = new ListItem<CellModel>(markupId, cellModelList.size());
        listItem.setMarkupId(markupId);
        listItem.setOutputMarkupId(true);
        listItem.add(new AttributeModifier("style", "left:" + cellModel.getPositionLeft() + "px; top:" + cellModel.getPositionTop() + "px;"));
        listItem.add(canvasItemPanel);
        canvasItemRepeater.add(listItem);

        // create the div with appropriate class within the DOM before we can ajax-update it
        String markup = "<div id=':id'></div>".replaceAll(":id", listItem.getMarkupId());
        String prepend = "$('#:container').append(\":markup\")".replaceAll(":container", plumbContainer.getMarkupId()).replaceAll(":markup", markup);
        target.prependJavaScript(prepend);

        // ajax-update the div
        target.add(listItem);

        // activate jsPlumb dragging on new canvas item
        target.appendJavaScript("makeCanvasItemPlumbDraggable(':itemId')".replaceAll(":itemId", "#" + listItem.getMarkupId()));
        if (!cellModel.getOutputVariableModelMap().isEmpty()) {
            target.appendJavaScript("addSourceEndpoint(':itemId')".replaceAll(":itemId", listItem.getMarkupId()));
        }
        if (!cellModel.getBindingModelMap().isEmpty()) {
            target.appendJavaScript("addTargetEndpoint(':itemId')".replaceAll(":itemId", listItem.getMarkupId()));
        }

        target.appendJavaScript("makeCanvasItemPlumbResizable(':itemId')".replaceAll(":itemId", "#" + listItem.getMarkupId()));

    }

    private Panel createCanvasItemPanel(CellModel cellModel) {
        CellType cellType = cellModel.getCellType();
        logger.info("createCanvasItemPanel for cell type " + cellType.getName());
        if ("FileUpload".equals(cellType.getName())) {
            return new FileUploadCanvasItemPanel("item", cellModel, cellCallbackHandler);
        } else if ("Script".equals(cellType.getName())) {
            return new ScriptCanvasItemPanel("item", cellModel, cellCallbackHandler);
        } else if ("PropertyCalculate".equals(cellType.getName())) {
            return new PropertyCalculateCanvasItemPanel("item", cellModel, cellCallbackHandler);
        } else if ("TableDisplay".equals(cellType.getName())) {
            return new TableDisplayCanvasItemPanel("item", cellModel, cellCallbackHandler);
        } else if ("ChemblActivitiesFetcher".equals(cellType.getName())) {
            return new ChemblActivitiesFetcherCanvasItemPanel("item", cellModel, cellCallbackHandler);
        } else if ("SdfUploader".equals(cellType.getName())) {
            return new SDFUploadCanvasItemPanel("item", cellModel, cellCallbackHandler);
        } else if ("CsvUploader".equals(cellType.getName())) {
            return new CSVUploadCanvasItemPanel("item", cellModel, cellCallbackHandler);
        } else if ("DatasetMerger".equals(cellType.getName())) {
            return new DatasetMergerCanvasItemPanel("item", cellModel, cellCallbackHandler);
        } else {
            return null;
        }
    }

    private void addCanvasItemDraggedBehavior() {
        AbstractDefaultAjaxBehavior onCanvasItemDragStopBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                String index = getRequest().getRequestParameters().getParameterValue(CANVASITEM_INDEX).toString();
                String x = getRequest().getRequestParameters().getParameterValue(POSITION_LEFT).toString();
                String y = getRequest().getRequestParameters().getParameterValue(POSITION_TOP).toString();

                logger.info("Item index " + index + " Dragged to: " + POSITION_LEFT + ": " + x + " " + POSITION_TOP + ": " + y);

                NotebookModel notebookModel = notebookSession.getCurrentNotebookModel();
                int i = Integer.parseInt(index);
                CellModel model = notebookModel.getCellModels()[i];
                model.setPositionLeft(Integer.parseInt(x));
                model.setPositionTop(Integer.parseInt(y));
                notebookSession.storeCurrentNotebook();
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                CharSequence callBackScript = getCallbackFunction(
                        CallbackParameter.explicit(CANVASITEM_INDEX),
                        CallbackParameter.explicit(POSITION_LEFT),
                        CallbackParameter.explicit(POSITION_TOP));
                callBackScript = "onNotebookCanvasItemDragged=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onCanvasItemDragStopBehavior);
    }

    private void addCanvasNewConnectionBehavior() {
        AbstractDefaultAjaxBehavior onNotebookCanvasNewConnectionBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                onNewCanvasConnection();
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                CharSequence callBackScript = getCallbackFunction(
                        CallbackParameter.explicit(SOURCE_ID),
                        CallbackParameter.explicit(TARGET_ID));
                callBackScript = "onNotebookCanvasNewConnection=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onNotebookCanvasNewConnectionBehavior);
    }

    private void onNewCanvasConnection() {
        String sourceMarkupId = getRequest().getRequestParameters().getParameterValue(SOURCE_ID).toString();
        String targetMarkupId = getRequest().getRequestParameters().getParameterValue(TARGET_ID).toString();
        System.out.println(sourceMarkupId + ", " + targetMarkupId);

        CellModel sourceCellModel = null;
        CellModel targetCellModel = null;

        Iterator<Component> iterator = canvasItemRepeater.iterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            if (sourceMarkupId.equals(component.getMarkupId())) {
                sourceCellModel = ((CanvasItemPanel) ((ListItem) component).get(0)).getCellModel();
            }
            if (targetMarkupId.equals(component.getMarkupId())) {
                targetCellModel = ((CanvasItemPanel) ((ListItem) component).get(0)).getCellModel();
            }
        }

        if (canApplyAutoBinding(sourceCellModel, targetCellModel)) {
            applyAutoBinding(sourceCellModel, targetCellModel);
        } else {
            connectionPanel.configure(sourceCellModel, targetCellModel);
            connectionPanel.setCanAddBindings(true);
            connectionPanel.showModal();
        }
    }

    private void applyAutoBinding(CellModel sourceCellModel, CellModel targetCellModel) {
        BindingModel bindingModel = targetCellModel.getBindingModelMap().values().iterator().next();
        VariableModel variableModel = sourceCellModel.getOutputVariableModelMap().values().iterator().next();
        bindingModel.setVariableModel(variableModel);
        logger.info("Auto-binding applied");
        notebookSession.storeCurrentNotebook();
        getRequestCycle().find(AjaxRequestTarget.class).add(NotebookCanvasPage.this);
    }

    private boolean canApplyAutoBinding(CellModel sourceCellModel, CellModel targetCellModel) {
        return (sourceCellModel != null) && (targetCellModel != null) && (sourceCellModel.getOutputVariableModelMap().size() == 1) && (targetCellModel.getBindingModelMap().size() == 1);
    }

    private void addConnectionsRenderBehavior() {
        AbstractDefaultAjaxBehavior behavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget ajaxRequestTarget) {

            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                String script = "jsPlumb.bind('ready', function() {";
                script += buildConnectionsJS();
                script += "})";
                response.render(OnDomReadyHeaderItem.forScript(script));
            }

        };
        add(behavior);

    }

    private String buildConnectionsJS() {
        StringBuilder stringBuilder = new StringBuilder();
        for (CellModel cellModel : canvasItemRepeater.getList()) {
            if (!cellModel.getOutputVariableModelMap().isEmpty()) {
                stringBuilder.append("addSourceEndpoint('" + CANVAS_ITEM_PREFIX + cellModel.getId() + "');\r\n");
            }
            if (!cellModel.getBindingModelMap().isEmpty()) {
                stringBuilder.append("addTargetEndpoint('" + CANVAS_ITEM_PREFIX + cellModel.getId() + "');\r\n");
            }
        }

        for (CellModel cellModel : canvasItemRepeater.getList()) {
            String targetId = CANVAS_ITEM_PREFIX + cellModel.getId();
            for (BindingModel bindingModel : cellModel.getBindingModelMap().values()) {
                if (bindingModel.getVariableModel() != null) {
                    String sourceId = CANVAS_ITEM_PREFIX + bindingModel.getVariableModel().getProducerCellModel().getId();
                    String js = "addConnection('" + sourceId + "', '" + targetId + "');\r\n";
                    stringBuilder.append(js);
                }
            }
        }
        return stringBuilder.toString();
    }
}
