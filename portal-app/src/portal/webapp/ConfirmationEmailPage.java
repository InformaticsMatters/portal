package portal.webapp;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.request.resource.CssResourceReference;
import toolkit.wicket.semantic.SemanticResourceReference;

public class ConfirmationEmailPage extends WebPage {

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/semantic-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/easygrid-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/lac.css")));
    }

    /* @Inject
    private ELFService elfService;

    public ConfirmationEmailPage(PageParameters parameters) {
        super(parameters);
        ResourceModel messageModel = null;
        StringValue token = parameters.get("token");
        String cssClass;
        try {
            elfService.confirmEmail(token.toString());
            messageModel = new ResourceModel("confirmationSuccessMessage");
            cssClass = "valid";
        } catch (Exception e) {
            messageModel = new ResourceModel("confirmationFailureMessage");
            cssClass = "invalid";
            e.printStackTrace();
        }
        Label confirmationMessage = new Label("confirmationMessage", messageModel);
        confirmationMessage.add(new AttributeModifier("class", cssClass));
        add(confirmationMessage);
    } */
}
