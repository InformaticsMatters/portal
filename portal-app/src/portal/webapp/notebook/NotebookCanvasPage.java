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
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookCanvasPage extends WebPage {

    public static final String DROP_DATA_TYPE = "dropDataType";
    public static final String DROP_DATA_ID = "dropDataId";
    public static final String POSITION_X = "positionX";
    public static final String POSITION_Y = "positionY";
    public static final String CANVASITEM_WICKETID = "canvasItem";
    private static final Logger logger = LoggerFactory.getLogger(NotebookCanvasPage.class);

    boolean cellsVisible = true;
    boolean canvasVisible = true;
    private AjaxLink cellsToggle;
    private AjaxLink canvasToggle;

    private NotebookCellsPanel notebookCellsPanel;
    private WebMarkupContainer plumbContainer;

    private List<Cell> cellList;
    private ListView<Cell> canvasItemRepeater;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private NotebooksSession notebooksSession;

    public NotebookCanvasPage() {
        notifierProvider.createNotifier(this, "notifier");
        addPanels();
        addActions();
        addCanvasDropBehavior();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/lac.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/notebook.js")));
        response.render(OnDomReadyHeaderItem.forScript("notebookDragAndDrop();"));
    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));

        notebookCellsPanel = new NotebookCellsPanel("cells");
        add(notebookCellsPanel);
        notebookCellsPanel.setOutputMarkupPlaceholderTag(true);

        plumbContainer = new WebMarkupContainer("plumbContainer");
        plumbContainer.setOutputMarkupId(true);
        plumbContainer.setOutputMarkupPlaceholderTag(true);
        add(plumbContainer);

        cellList = new ArrayList<>();
        canvasItemRepeater = new ListView<Cell>(CANVASITEM_WICKETID, cellList) {

            @Override
            protected void populateItem(ListItem<Cell> components) {
                // we manage items manually when dropping or removing them from the Canvas
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
                callBackScript = "onNotebookCanvasDrop=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onCanvasDropBehavior);
    }

    private void addCanvasItem(AjaxRequestTarget target) {
        String dropDataType = getRequest().getRequestParameters().getParameterValue(DROP_DATA_TYPE).toString();
        String dropDataId = getRequest().getRequestParameters().getParameterValue(DROP_DATA_ID).toString();
        String x = getRequest().getRequestParameters().getParameterValue(POSITION_X).toString();
        String y = getRequest().getRequestParameters().getParameterValue(POSITION_Y).toString();

        logger.info("Type: " + dropDataType + " ID: " + dropDataId + " at " + POSITION_X + ": " + x + " " + POSITION_Y + ": " + y);

        Cell cell = null;
        Panel canvasItemPanel = null;

        if (CellType.NOTEBOOK_DEBUG.toString().equals(dropDataId)) {
            cell = new NotebookDebugCell();
            cell.setX(Integer.parseInt(x));
            cell.setY(Integer.parseInt(y));
            cellList.add(cell);
            canvasItemPanel = new NotebookDebugCanvasItemPanel("item", notebooksSession.retrievePocNotebookDescriptor(), (NotebookDebugCell) cell);
        } else if (CellType.FILE_UPLOAD.toString().equals(dropDataId)) {
        } else if (CellType.CODE.toString().equals(dropDataId)) {
        }

        if (cell != null) {
            ListItem listItem = new ListItem(dropDataType + dropDataId, cellList.size());
            listItem.setOutputMarkupId(true);
            listItem.add(new AttributeModifier("style", "top:" + cell.getX() + "px; left:" + cell.getY() + "px;"));
            listItem.add(canvasItemPanel);
            canvasItemRepeater.add(listItem);

            // create the div within the DOM before we can ajax-update it
            String markup = "<div class='canvas-item' id=':id'></div>".replaceAll(":id", listItem.getMarkupId());
            String prepend = "$('#:container').append(\":markup\")".replaceAll(":container", plumbContainer.getMarkupId()).replaceAll(":markup", markup);
            target.prependJavaScript(prepend);

            // ajax-update the div
            target.add(listItem);
        }
    }
}
