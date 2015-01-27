package portal.webapp;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.request.resource.CssResourceReference;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class UserCreatedPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/semantic-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/easygrid-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/lac.css")));
    }

   /* public UserCreatedPage(String afterActionMessage, ApplicationUser user, boolean warningStyle) {
        Label afterActionMessageLabel = new Label("userRegistered", afterActionMessage);
        if (warningStyle) {
            afterActionMessageLabel.add(new AttributeModifier("class", "invalid"));
        } else {
            afterActionMessageLabel.add(new AttributeModifier("class", "valid"));
        }
        add(afterActionMessageLabel);

        Label emailMessageLabel = new Label("email", user.getEmail());
        add(emailMessageLabel);

        Label tokenMessageLabel = new Label("duetime", "Please check your inbox and click the link to confirm your email within " + TokenType.CONFIRM_EMAIL.getHoursValid() + " hs.");
        add(tokenMessageLabel);
    } */

}
