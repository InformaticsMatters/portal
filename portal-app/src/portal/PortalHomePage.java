package portal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import portal.notebook.NotebookCanvasPage;
import portal.workflow.WorkflowPage;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class PortalHomePage extends WebPage implements SecuredComponent {

    @Inject
    private NotifierProvider notifierProvider;

    public PortalHomePage() {
        notifierProvider.createNotifier(this, "notifier");
        addPanels();
        addActions();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/lac.css")));
    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));
    }

    private void addActions() {

        AjaxLink workflowLink = new AjaxLink("workflow") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(WorkflowPage.class);
            }
        };
        add(workflowLink);

        AjaxLink notebookLink = new AjaxLink("notebook") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(NotebookCanvasPage.class);
            }
        };
        add(notebookLink);
    }
}
