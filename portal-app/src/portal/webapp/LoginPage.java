package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.request.resource.CssResourceReference;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class LoginPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    private AjaxLink userRegistrationLink;

    public LoginPage() {
        notifierProvider.createNotifier(this, "notifier");
        addActions();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/semantic-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/easygrid-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/lac.css")));
    }

    private void addActions() {
        userRegistrationLink = new AjaxLink("userRegistration") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(UserRegistrationPage.class);
            }
        };
        add(userRegistrationLink);
    }
}
