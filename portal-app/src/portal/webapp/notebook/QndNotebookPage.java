package portal.webapp.notebook;


import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import portal.webapp.FooterPanel;
import portal.webapp.MenuPanel;
import portal.webapp.PortalHomePage;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

/**
 * @author simetrias
 */

public class QndNotebookPage extends WebPage {
    @Inject
    private NotebooksSession notebooksSession;

    public QndNotebookPage() {
        addPanels();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalHomePage.class, "resources/lac.js")));
    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));
        addPoc();
    }


    private void addPoc() {
        Notebook notebook = notebooksSession.retrievePocNotebook();
        QndNotebookPanel qndNotebookPanel = new QndNotebookPanel("notebook", notebook);
        qndNotebookPanel.setOutputMarkupId(true);
        add(qndNotebookPanel);
    }



}
