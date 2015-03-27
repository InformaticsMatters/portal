package portal.webapp;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import toolkit.wicket.semantic.SemanticResourceReference;

import java.io.Serializable;

public class ForgotPasswordPage extends WebPage {

   /* @Inject
    private ELFService elfService;

    public ForgotPasswordPage() {
        final Model<String> feedbackMessageModel = new Model<String>("");
        final Label feedbackMessage = new Label("feedbackMessage", feedbackMessageModel);
        feedbackMessage.setOutputMarkupId(true);
        feedbackMessage.setOutputMarkupPlaceholderTag(true);
        feedbackMessage.setVisible(false);
        add(feedbackMessage);

        final Link<String> goToLoginPage = new Link<String>("goToLoginPage") {
            @Override
            public void onClick() {
                setResponsePage(LoginPage.class);
            }
        };
        goToLoginPage.setOutputMarkupId(true);
        goToLoginPage.setOutputMarkupPlaceholderTag(true);
        goToLoginPage.setVisible(false);
        add(goToLoginPage);

        final FormModel model = new FormModel();
        Form<FormModel> form = new Form<FormModel>("form", new CompoundPropertyModel<FormModel>(model));
        form.setOutputMarkupId(true);
        form.add(new TextField<String>("usernameOrEmailAddress"));

        form.add(new AjaxSubmitLink("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                feedbackMessage.setVisible(true);
                String usernameOrEmail = model.getUsernameOrEmailAddress();
                boolean userMatch = false;
                if (usernameOrEmail != null) {
                    userMatch = elfService.postPasswordRecoveryEmail(usernameOrEmail);
                }
                if (userMatch) {
                    form.setVisible(false);
                    goToLoginPage.setVisible(true);
                    feedbackMessage.add(new AttributeModifier("class", "valid"));
                    feedbackMessageModel.setObject(getLocalizer().getString("emailSent", ForgotPasswordPage.this));
                    target.add(goToLoginPage);
                    target.add(form);
                } else {
                    feedbackMessage.add(new AttributeModifier("class", "invalid"));
                    feedbackMessageModel.setObject(getLocalizer().getString("usernameOrEmailNotFound", ForgotPasswordPage.this));
                }
                target.add(feedbackMessage);
            }
        });
        add(form);
    }  */

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
    }

    class FormModel implements Serializable {

        private String usernameOrEmailAddress;

        public String getUsernameOrEmailAddress() {
            return usernameOrEmailAddress;
        }

        public void setUsernameOrEmailAddress(String usernameOrEmailAddress) {
            this.usernameOrEmailAddress = usernameOrEmailAddress;
        }
    }
}
