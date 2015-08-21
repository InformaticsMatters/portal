package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

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
        AjaxLink datasetsLink = new AjaxLink("chemcentral") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(ChemcentralPage.class);
            }
        };
        add(datasetsLink);

        AjaxLink workflowLink = new AjaxLink("workflow") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(WorkflowPage.class);
            }
        };
        add(workflowLink);
    }
}
