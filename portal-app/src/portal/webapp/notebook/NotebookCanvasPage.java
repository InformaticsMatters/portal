package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import portal.webapp.FooterPanel;
import portal.webapp.MenuPanel;
import portal.webapp.PortalHomePage;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

/**
 * @author simetrias
 */

public class NotebookCanvasPage extends WebPage {

    public static final String DROP_DATA_TYPE = "dropDataType";
    public static final String DROP_DATA_ID = "dropDataId";

    boolean cellsVisible = true;
    boolean canvasVisible = true;
    private AjaxLink cellsToggle;
    private AjaxLink canvasToggle;
    private NotebookCellsPanel notebookCellsPanel;
    private NotebookCanvasPanel notebookCanvasPanel;

    @Inject
    private NotifierProvider notifierProvider;

    public NotebookCanvasPage() {
        notifierProvider.createNotifier(this, "notifier");
        addPanels();
        addActions();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/dom.jsPlumb-1.7.5.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/Canvas.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/lac.js")));
        response.render(OnDomReadyHeaderItem.forScript("init(); notebookDragAndDrop();"));
    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));

        notebookCellsPanel = new NotebookCellsPanel("cells");
        add(notebookCellsPanel);
        notebookCellsPanel.setOutputMarkupPlaceholderTag(true);

        notebookCanvasPanel = new NotebookCanvasPanel("notebookCanvas");
        add(notebookCanvasPanel);
        notebookCanvasPanel.setOutputMarkupPlaceholderTag(true);
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

}
