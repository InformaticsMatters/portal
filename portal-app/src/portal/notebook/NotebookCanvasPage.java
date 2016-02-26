package portal.notebook;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.FooterPanel;
import portal.MenuPanel;
import portal.PopupContainerProvider;
import portal.PortalHomePage;
import portal.notebook.api.*;
import portal.notebook.cells.*;
import portal.notebook.service.NotebookInfo;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import java.util.ArrayList;
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
    public static final String SIZE_WIDTH = "sizeWidth";
    public static final String SIZE_HEIGHT = "sizeHeight";
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
    private WebMarkupContainer plumbContainer;

    private ListView<Long> canvasItemRepeater;

    private EditNotebookPanel editNotebookPanel;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public NotebookCanvasPage() {
        notifierProvider.createNotifier(this, "notifier");
        popupContainerProvider.createPopupContainerForPage(this, "modalPopupContainer");
        setOutputMarkupId(true);
        addMenuAndFooter();
        addPlumbContainer();
        addCanvasItemRepeater();
        addActions();
        addCanvasPaletteDropBehavior();
        addCanvasItemDraggedBehavior();
        addCanvasNewConnectionBehavior();
        addConnectionsRenderBehavior();
        addResizeBehavior();
        NotebookInfo notebookInfo = notebookSession.prepareDefaultNotebook();
        addEditNotebookPanel();
        addNotebookListPanel();
        addNotebookCellTypesPanel();
        notebookSession.loadCurrentNotebook(notebookInfo.getId());
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
        response.render(OnDomReadyHeaderItem.forScript("makeCanvasItemPlumbDraggable('.notebook-canvas-item');"));
    }

    private void addMenuAndFooter() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));

        add(new Label("notebookName", new PropertyModel(notebookSession, "currentNotebookInfo.name")));
        add(new Label("notebookOwner", new PropertyModel(notebookSession, "currentNotebookInfo.owner")));
    }

    private void addPlumbContainer() {
        plumbContainer = new WebMarkupContainer("plumbContainer");
        plumbContainer.setOutputMarkupId(true);
        plumbContainer.setOutputMarkupPlaceholderTag(true);
        add(plumbContainer);
    }

    private void addCanvasItemRepeater() {
        IModel<List<Long>> listModel = new IModel<List<Long>>() {

            @Override
            public List<Long> getObject() {
                if (notebookSession.getCurrentNotebookInstance() == null) {
                    return new ArrayList<>();
                } else {
                    List<CellInstance> cellInstanceList = notebookSession.getCurrentNotebookInstance().getCellList();
                    List<Long> idList = new ArrayList<>();
                    for (CellInstance cellInstance : cellInstanceList) {
                        idList.add(cellInstance.getId());
                    }
                    return idList;
                }
            }

            @Override
            public void setObject(List<Long> cellIds) {

            }

            @Override
            public void detach() {

            }
        };
        canvasItemRepeater = new ListView<Long>("canvasItem", listModel) {

            @Override
            protected void populateItem(ListItem<Long> listItem) {
                CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellById(listItem.getModelObject());
                String markupId = CANVAS_ITEM_PREFIX + cellInstance.getId();
                Panel canvasItemPanel = createCanvasItemPanel(cellInstance);
                listItem.setOutputMarkupId(true);
                listItem.setMarkupId(markupId);
                listItem.add(canvasItemPanel);
            }
        };
        canvasItemRepeater.setOutputMarkupId(true);
        plumbContainer.add(canvasItemRepeater);
    }

    private void addNotebookCellTypesPanel() {
        NotebookCellTypesPanel notebookCellTypesPanel = new NotebookCellTypesPanel("descriptors");
        add(notebookCellTypesPanel);
        notebookCellTypesPanel.setOutputMarkupPlaceholderTag(true);
    }

    private void addEditNotebookPanel() {
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
    }

    private void addNotebookListPanel() {
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

        CellDefinition cellDefinition = notebookSession.findCellType(dropDataId);
        CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().addCell(cellDefinition);
        cellInstance.setPositionLeft(Integer.parseInt(x));
        cellInstance.setPositionTop(Integer.parseInt(y));
        notebookSession.storeCurrentNotebook();

        Panel canvasItemPanel = createCanvasItemPanel(cellInstance);

        List<CellInstance> cellInstanceList = notebookSession.getCurrentNotebookInstance().getCellList();
        String markupId = CANVAS_ITEM_PREFIX + cellInstance.getId();
        ListItem<CellInstance> listItem = new ListItem<>(markupId, cellInstanceList.size());
        listItem.setMarkupId(markupId);
        listItem.setOutputMarkupId(true);
        listItem.add(canvasItemPanel);
        canvasItemRepeater.add(listItem);

        // create the div with appropriate class within the DOM before we can ajax-update it
        String markup = "<div id=':id'></div>".replaceAll(":id", listItem.getMarkupId());
        String prepend = "$('#:container').append(\":markup\")".replaceAll(":container", plumbContainer.getMarkupId()).replaceAll(":markup", markup);
        target.prependJavaScript(prepend);

        // ajax-update the div
        target.add(listItem);

        // activate jsPlumb dragging on newly created canvas item
        target.appendJavaScript("makeCanvasItemPlumbDraggable(':itemId')".replaceAll(":itemId", "#" + listItem.getMarkupId()));

        // create connection endpoints
        target.appendJavaScript(buildEndpointsJS(cellInstance));

        // repaint everything jsPlumb related for that cell (fixes Continuous anchor issues)
        target.appendJavaScript("jsPlumb.repaintEverything(':itemId')".replaceAll(":itemId", "#" + listItem.getMarkupId()));
    }

    private Panel createCanvasItemPanel(CellInstance cellInstance) {
        CellDefinition cellType = cellInstance.getCellDefinition();
        logger.info("createCanvasItemPanel for cell type " + cellType.getName());
        if ("TableDisplay".equals(cellType.getName())) {
            return new TableDisplayCanvasItemPanel("item", cellInstance.getId());
        } else if (ChemblActivitiesFetcherCellDefinition.CELL_NAME.equals(cellType.getName())) {
            return new DefaultCanvasItemPanel("item", cellInstance.getId());
        } else if (SdfUploadCellDefinition.CELL_NAME.equals(cellType.getName())) {
            //return new SDFUploadCanvasItemPanel("item", cellInstance.getId());
            return new DefaultCanvasItemPanel("item", cellInstance.getId());
        } else if (CsvUploadCellDefinition.CELL_NAME.equals(cellType.getName())) {
            //return new CSVUploadCanvasItemPanel("item", cellInstance.getId());
            return new DefaultCanvasItemPanel("item", cellInstance.getId());
        } else if (DatasetMergerCellDefinition.CELL_NAME.equals(cellType.getName())) {
            return new DatasetMergerCanvasItemPanel("item", cellInstance.getId());
        } else if (ConvertToMoleculesCellDefinition.CELL_NAME.equals(cellType.getName())) {
            return new ConvertToMoleculesCanvasItemPanel("item", cellInstance.getId());
        } else if ("TransformValues".equals(cellType.getName())) {
            return new TransformValuesCanvasItemPanel("item", cellInstance.getId());
        } else if ("TrustedGroovyDatasetScript".equals(cellType.getName())) {
            return new GroovyScriptTrustedCanvasItemPanel("item", cellInstance.getId());
        } else {
            logger.warn("cell type " + cellType.getName() + " not recognised");
            return new DefaultCanvasItemPanel("item", cellInstance.getId());
        }
    }

    private void addCanvasItemDraggedBehavior() {
        AbstractDefaultAjaxBehavior onCanvasItemDragStopBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                String index = getRequest().getRequestParameters().getParameterValue(CANVASITEM_INDEX).toString();
                String x = getRequest().getRequestParameters().getParameterValue(POSITION_LEFT).toString();
                String y = getRequest().getRequestParameters().getParameterValue(POSITION_TOP).toString();

                logger.info("Item index " + index + " dragged to: " + POSITION_LEFT + ": " + x + " " + POSITION_TOP + ": " + y);

                NotebookInstance notebookModel = notebookSession.getCurrentNotebookInstance();
                int i = Integer.parseInt(index);
                CellInstance model = notebookModel.getCellList().get(i);
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

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setWicketAjaxResponse(false);
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

    private void addResizeBehavior() {
        AbstractDefaultAjaxBehavior onNotebookCanvasItemResizedBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                String index = getRequest().getRequestParameters().getParameterValue(CANVASITEM_INDEX).toString();
                String width = getRequest().getRequestParameters().getParameterValue(SIZE_WIDTH).toString();
                String height = getRequest().getRequestParameters().getParameterValue(SIZE_HEIGHT).toString();

                logger.info("Item index " + index + " resized to: " + SIZE_WIDTH + ": " + width + " and " + SIZE_HEIGHT + ": " + height);

                NotebookInstance notebookModel = notebookSession.getCurrentNotebookInstance();
                int i = Integer.parseInt(index);
                CellInstance model = notebookModel.getCellList().get(i);
                model.setSizeWidth(Integer.parseInt(width));
                model.setSizeHeight(Integer.parseInt(height));
                notebookSession.storeCurrentNotebook();
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                CharSequence callBackScript = getCallbackFunction(
                        CallbackParameter.explicit(CANVASITEM_INDEX),
                        CallbackParameter.explicit(SIZE_WIDTH),
                        CallbackParameter.explicit(SIZE_HEIGHT));
                callBackScript = "onNotebookCanvasItemResized=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setWicketAjaxResponse(false);
            }
        };
        add(onNotebookCanvasItemResizedBehavior);
    }

    private void onNewCanvasConnection() {
        String sourceEndpointUuid = getRequest().getRequestParameters().getParameterValue(SOURCE_ID).toString();
        String targetEndpointUuid = getRequest().getRequestParameters().getParameterValue(TARGET_ID).toString();
        String sourceCellMarkupId = sourceEndpointUuid.substring(0, sourceEndpointUuid.indexOf("-"));
        String targetCellMarkupId = targetEndpointUuid.substring(0, targetEndpointUuid.indexOf("-"));
        String sourceVariableName = sourceEndpointUuid.substring(sourceEndpointUuid.indexOf("-") + 1);
        String targetVariableName = targetEndpointUuid.substring(targetEndpointUuid.indexOf("-") + 1);

        CellInstance sourceCellInstance = null;
        CellInstance targetCellInstance = null;
        VariableInstance source = null;
        BindingInstance target = null;

        Iterator<Component> iterator = canvasItemRepeater.iterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            CanvasItemPanel canvasItemPanel = (CanvasItemPanel) ((ListItem) component).get(0);
            if (sourceCellMarkupId.equals(component.getMarkupId())) {
                sourceCellInstance = canvasItemPanel.getCellInstance();
                source = sourceCellInstance.getOutputVariableMap().get(sourceVariableName);
            }
            if (targetCellMarkupId.equals(component.getMarkupId())) {
                targetCellInstance = canvasItemPanel.getCellInstance();
                target = targetCellInstance.getBindingMap().get(targetVariableName);
            }
        }
        if (source != null && target != null) {
            applyBinding(source, target);
        }
    }

    private void applyBinding(VariableInstance variableInstance, BindingInstance bindingInstance) {
        bindingInstance.setVariable(variableInstance);
        logger.info("Binding applied");
        notebookSession.storeCurrentNotebook();
        getRequestCycle().find(AjaxRequestTarget.class).add(NotebookCanvasPage.this);
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
                script += "; jsPlumb.repaintEverything();";
                script += "})";
                response.render(OnDomReadyHeaderItem.forScript(script));
            }

        };
        add(behavior);
    }

    private String buildConnectionsJS() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Long cellId : canvasItemRepeater.getList()) {
            CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellById(cellId);
            stringBuilder.append(buildEndpointsJS(cellInstance));
        }
        for (Long cellId : canvasItemRepeater.getList()) {
            CellInstance targetCellInstance = notebookSession.getCurrentNotebookInstance().findCellById(cellId);
            String targetCellMarkupId = CANVAS_ITEM_PREFIX + targetCellInstance.getId();
            for (BindingInstance bindingInstance : targetCellInstance.getBindingMap().values()) {
                VariableInstance source = bindingInstance.getVariable();
                if (source != null) {
                    String sourceEndpointUuid = CANVAS_ITEM_PREFIX + source.getCellId() + "-" + source.getName();
                    String targetEndpointUuid = targetCellMarkupId + "-" + bindingInstance.getName();
                    String js = "addConnection('" + sourceEndpointUuid + "', '" + targetEndpointUuid + "');";
                    stringBuilder.append(js);
                }
            }
        }
        return stringBuilder.toString();
    }

    private String buildEndpointsJS(CellInstance cellInstance) {
        String itemId = CANVAS_ITEM_PREFIX + cellInstance.getId();
        StringBuilder stringBuilder = new StringBuilder();
        for (VariableInstance variableInstance : cellInstance.getOutputVariableMap().values()) {
            String endpointId = itemId + "-" + variableInstance.getName();
            stringBuilder.append("addSourceEndpoint('" + itemId + "', '" + endpointId + "', '" + variableInstance.getName() + "');");
        }
        for (BindingInstance bindingInstance : cellInstance.getBindingMap().values()) {
            String endpointId = itemId + "-" + bindingInstance.getName();
            stringBuilder.append("addTargetEndpoint('" + itemId + "', '" + endpointId + "', '" + bindingInstance.getName() + "');");
        }
        return stringBuilder.toString();
    }
}
