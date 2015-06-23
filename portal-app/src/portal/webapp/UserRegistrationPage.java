package portal.webapp;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class UserRegistrationPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;

    public UserRegistrationPage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addUserRegistrationPanel();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
    }

    private void addUserRegistrationPanel() {
        UserRegistrationPanel userRegistrationPanel = new UserRegistrationPanel("userRegistrationPanel");
        add(userRegistrationPanel);
    }

}
