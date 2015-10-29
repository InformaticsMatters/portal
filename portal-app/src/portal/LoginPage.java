package portal;

import com.im.lac.services.user.User;
import com.im.lac.user.client.UserClient;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

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
        StatelessForm<LoginData> loginForm = new StatelessForm<>("loginForm");
        loginForm.setModel(new CompoundPropertyModel<>(new LoginData()));

        loginForm.add(new TextField("userName"));
        loginForm.add(new PasswordTextField("password"));

        AjaxSubmitLink submitLink = new IndicatingAjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                initUserSession(loginForm.getModelObject());
                continueToOriginalDestination();
            }
        };
        loginForm.add(submitLink);

        add(loginForm);
    }

    private void initUserSession(LoginData loginData) {
        UserClient userClient = new UserClient();
        try {
            User user = userClient.getUserObject(loginData.getUserName());
            sessionContext.setLoggedInUser(user.getUsername());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class LoginData implements Serializable {

        private String userName;
        private String password;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
