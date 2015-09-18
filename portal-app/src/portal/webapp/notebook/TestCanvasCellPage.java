package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import portal.webapp.PortalHomePage;
import toolkit.wicket.semantic.SemanticResourceReference;

/**
 * Created by mariapaz on 9/18/15.
 */
public class TestCanvasCellPage extends WebPage {

    private WebMarkupContainer plumbContainer;

    private AjaxLink fileButton;

    private FileUploadCanvasItemPanel fileUploadCanvasItemPanel;
    private GroovyScriptCanvasItemPanel groovyScriptCanvasItemPanel;
    private TableViewCanvasItemPanel tableViewCanvasItemPanel;

    public TestCanvasCellPage() {
        addCanvas();
        addButtons();
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
        response.render(OnDomReadyHeaderItem.forScript("init();"));
    }

    private void addCanvas() {
        plumbContainer = new WebMarkupContainer("plumbContainer");
        plumbContainer.setOutputMarkupId(true);
        plumbContainer.setOutputMarkupPlaceholderTag(true);
        add(plumbContainer);

        fileUploadCanvasItemPanel = new FileUploadCanvasItemPanel("fileUploadItem");
        plumbContainer.add(fileUploadCanvasItemPanel);
        fileUploadCanvasItemPanel.setOutputMarkupPlaceholderTag(true);
        fileUploadCanvasItemPanel.setVisible(false);

        groovyScriptCanvasItemPanel = new GroovyScriptCanvasItemPanel("groovyScriptItem");
        plumbContainer.add(groovyScriptCanvasItemPanel);
        groovyScriptCanvasItemPanel.setOutputMarkupPlaceholderTag(true);
        groovyScriptCanvasItemPanel.setVisible(false);

        tableViewCanvasItemPanel = new TableViewCanvasItemPanel("tableViewItem");
        plumbContainer.add(tableViewCanvasItemPanel);
        tableViewCanvasItemPanel.setOutputMarkupPlaceholderTag(true);
        tableViewCanvasItemPanel.setVisible(false);
    }

    private void addButtons() {
        fileButton = new AjaxLink("fileButton") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                fileUploadCanvasItemPanel.setVisible(true);
                target.add(fileUploadCanvasItemPanel);
                target.appendJavaScript("makeCanvasItemsDraggable('" + fileUploadCanvasItemPanel.getMarkupId() + "')");
                target.appendJavaScript("addSourceEndpoint('" + fileUploadCanvasItemPanel.getMarkupId() + "')");
                target.appendJavaScript("addTargetEndpoint('" + fileUploadCanvasItemPanel.getMarkupId() + "')");
            }
        };
        add(fileButton);
    }
}
