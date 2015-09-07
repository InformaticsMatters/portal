package portal.webapp.registration;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import toolkit.wicket.semantic.SemanticResourceReference;

public class ConfirmationEmailPage extends WebPage {

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
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
