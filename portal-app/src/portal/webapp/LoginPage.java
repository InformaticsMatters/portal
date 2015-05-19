package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class LoginPage extends WebPage {

    @Inject
    private SessionContext sessionContext;
    @Inject
    private NotifierProvider notifierProvider;

    public LoginPage() {
        notifierProvider.createNotifier(this, "notifier");
        addLoginForm();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
    }

    private void addLoginForm() {
        StatelessForm loginForm = new StatelessForm("loginForm");
        AjaxSubmitLink submitLink = new IndicatingAjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                sessionContext.setLoggedInUser("loggedIn");
                continueToOriginalDestination();
            }
        };
        loginForm.add(submitLink);

        AjaxLink userRegistrationLink = new AjaxLink("userRegistration") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(UserRegistrationPage.class);
            }
        };
        loginForm.add(userRegistrationLink);

        add(loginForm);
    }
}
