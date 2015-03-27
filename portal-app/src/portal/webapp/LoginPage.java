package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
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
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
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
