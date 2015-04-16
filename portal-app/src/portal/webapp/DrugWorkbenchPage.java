package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import portal.service.api.DatasetService;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class DrugWorkbenchPage extends WebPage {

    private AjaxLink rightSidebar;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService service;

    public DrugWorkbenchPage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addActions();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/lac.js")));
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
    }

    private void addActions() {

        rightSidebar = new AjaxLink("rightSidebar") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                ajaxRequestTarget.appendJavaScript("rightSideBarToggle()");
            }
        };
        add(rightSidebar);
    }

}
