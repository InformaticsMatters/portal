package portal.webapp.notebook;


import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import portal.webapp.FooterPanel;
import portal.webapp.MenuPanel;
import portal.webapp.WorkflowPage;
import toolkit.wicket.semantic.SemanticResourceReference;

/**
 * @author simetrias
 */

public class NotebookPage extends WebPage {

    public NotebookPage() {
        addPanels();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(WorkflowPage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(WorkflowPage.class, "resources/lac.js")));
    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));
    }
}
