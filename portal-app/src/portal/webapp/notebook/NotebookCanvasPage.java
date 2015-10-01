package portal.webapp.notebook;

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
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.webapp.FooterPanel;
import portal.webapp.MenuPanel;
import portal.webapp.PortalHomePage;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookCanvasPage extends WebPage {

    public static final String DROP_DATA_TYPE = "dropDataType";
    public static final String DROP_DATA_ID = "dropDataId";
    public static final String POSITION_LEFT = "positionX";
    public static final String POSITION_TOP = "positionY";
    public static final String CANVASITEM_WICKETID = "canvasItem";
    public static final String CANVASITEM_INDEX = "index";
    private static final Logger logger = LoggerFactory.getLogger(NotebookCanvasPage.class);

    boolean cellsVisible = true;
    boolean canvasVisible = true;
    private AjaxLink cellsToggle;
    private AjaxLink canvasToggle;

    private NotebookCellDescriptorsPanel notebookCellDescriptorsPanel;
    private WebMarkupContainer plumbContainer;

    private ListView<Cell> canvasItemRepeater;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private NotebooksSession notebooksSession;
    private transient Notebook notebook;
    private int initialItemCount;

    public NotebookCanvasPage() {
        notifierProvider.createNotifier(this, "notifier");
        notebook = notebooksSession.retrievePocNotebook();
        addPanels();
        addActions();
        addCanvasPaletteDropBehavior();
        addCanvasItemDraggedBehavior();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/dom.jsPlumb-1.7.5.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/lac.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/notebook.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/notebook.js")));
        response.render(OnDomReadyHeaderItem.forScript("initJsPlumb(); addCellsPaletteDragAndDropSupport();"));
        response.render(OnDomReadyHeaderItem.forScript("makeCanvasItemPlumbDraggable('.notebook-canvas-item');"));
    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));

        notebookCellDescriptorsPanel = new NotebookCellDescriptorsPanel("cells");
        add(notebookCellDescriptorsPanel);
        notebookCellDescriptorsPanel.setOutputMarkupPlaceholderTag(true);

        plumbContainer = new WebMarkupContainer("plumbContainer");
        plumbContainer.setOutputMarkupId(true);
        plumbContainer.setOutputMarkupPlaceholderTag(true);
        add(plumbContainer);

        IModel<List<Cell>> listModel = new IModel<List<Cell>>() {
            @Override
            public List<Cell> getObject() {
                return notebook.getCellList();
            }

            @Override
            public void setObject(List<Cell> cells) {

            }

            @Override
            public void detach() {

            }
        };
        initialItemCount = 0;
        canvasItemRepeater = new ListView<Cell>(CANVASITEM_WICKETID, listModel) {

            @Override
            protected void populateItem(ListItem<Cell> listItem) {
                initialItemCount++;
                Cell cell = listItem.getModelObject();
                Panel canvasItemPanel = createCanvasItemPanel(cell);
                listItem.setOutputMarkupId(true);
                listItem.setMarkupId(CANVASITEM_WICKETID + initialItemCount);
                listItem.add(new AttributeModifier("style", "left:" + cell.getPositionLeft() + "px; top:" + cell.getPositionTop() + "px;"));
                listItem.add(canvasItemPanel);
            }
        };
        canvasItemRepeater.setOutputMarkupId(true);
        plumbContainer.add(canvasItemRepeater);
    }

    private void refreshPanelsVisibility(AjaxRequestTarget target) {
        target.appendJavaScript("applyNotebookCanvasPageLayout('" + cellsVisible + "', '" + canvasVisible + "')");
    }

    private void addActions() {
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

        CellType cellType = CellType.valueOf(dropDataId);
        Cell cell = createCell(cellType);

        if (cell != null) {

            cell.setPositionLeft(Integer.parseInt(x));
            cell.setPositionTop(Integer.parseInt(y));
            notebook.addCell(cell);

            Panel canvasItemPanel = createCanvasItemPanel(cell);

            List<Cell> cellList = notebook.getCellList();
            ListItem listItem = new ListItem(CANVASITEM_WICKETID + cellList.size(), cellList.size());
            listItem.setOutputMarkupId(true);
            listItem.add(new AttributeModifier("style", "left:" + cell.getPositionLeft() + "px; top:" + cell.getPositionTop() + "px;"));
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

            notebooksSession.saveNotebook(notebook);
        }
    }

    private Panel createCanvasItemPanel(Cell cell) {
        CellType cellType = cell.getCellType();
        if (CellType.NOTEBOOK_DEBUG.equals(cellType)) {
            return new NotebookDebugCanvasItemPanel("item", notebook, (NotebookDebugCell) cell);
        } else if (CellType.FILE_UPLOAD.equals(cellType)) {
            return new FileUploadCanvasItemPanel("item", notebook, (FileUploadCell) cell);
        } else if (CellType.CODE.equals(cellType)) {
            return new ScriptCanvasItemPanel("item", notebook, (ScriptCell) cell);
        } else if (CellType.PROPERTY_CALCULATE.equals(cellType)) {
            return new PropertyCalculateCanvasItemPanel("item", notebook, (PropertyCalculateCell) cell);
        } else if (CellType.TABLE_DISPLAY.equals(cellType)) {
            return new TableDisplayCanvasItemPanel("item", notebook, (TableDisplayCell) cell);
        } else {
            return null;
        }
    }

    private Cell createCell(CellType cellType) {
        if (CellType.NOTEBOOK_DEBUG.equals(cellType)) {
            return new NotebookDebugCell();
        } else if (CellType.FILE_UPLOAD.equals(cellType)) {
            return new FileUploadCell();
        } else if (CellType.CODE.equals(cellType)) {
            return new ScriptCell();
        } else if (CellType.PROPERTY_CALCULATE.equals(cellType)) {
            return new PropertyCalculateCell();
        } else if (CellType.TABLE_DISPLAY.equals(cellType)) {
            return new TableDisplayCell();
        }  else {
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

                int i = Integer.parseInt(index);
                Cell model = notebook.getCellList().get(i);
                model.setPositionLeft(Integer.parseInt(x));
                model.setPositionTop(Integer.parseInt(y));
                notebooksSession.saveNotebook(notebook);
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
}
