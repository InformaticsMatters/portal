package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.CompoundPropertyModel;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class UserRegistrationPage extends WebPage {

    private static final Integer CAPTCHA_MIN_LENGTH = 6;
    private static final Integer CAPTCHA_MAX_LENGTH = 8;
    @Inject
    private NotifierProvider notifierProvider;
    private Form<UserRegistrationData> form;
    private String captchaPassword;
    private NonCachingImage captchaImage;
    private TextField<String> captchaText;

    public UserRegistrationPage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addForm();
    }

    private static String generateRandomCaptchaString(int minLength, int maxLength) {
        int length = randomInt(minLength, maxLength);
        byte b[] = new byte[length];
        for (int index = 0; index < length; index++)
            b[index] = (byte) randomInt('a', 'z');
        return new String(b);
    }

    private static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min) + min);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
    }

    private void addForm() {
        form = new Form<UserRegistrationData>("form");
        form.setOutputMarkupId(true);
        form.setModel(new CompoundPropertyModel<UserRegistrationData>(new UserRegistrationData()));
        add(form);

        TextField<String> usernameField = new TextField<String>("username");
        form.add(usernameField);

        TextField<String> emailField = new TextField<String>("email");
        form.add(emailField);

        TextField<Integer> phoneNumberField = new TextField<Integer>("phoneNumber");
        form.add(phoneNumberField);

        TextField<String> firstNameField = new TextField<String>("firstName");
        form.add(firstNameField);

        TextField<String> lastNameField = new TextField<String>("lastName");
        form.add(lastNameField);

        PasswordTextField password = new PasswordTextField("password");
        password.setOutputMarkupId(true);
        password.setRequired(false);
        password.setResetPassword(false);
        form.add(password);

        PasswordTextField passwordConfirmation = new PasswordTextField("passwordConfirmation");
        passwordConfirmation.setOutputMarkupId(true);
        passwordConfirmation.setRequired(false);
        passwordConfirmation.setResetPassword(false);
        form.add(passwordConfirmation);

        addOrReplaceCaptcha();
        form.add(new AjaxLink("refreshCaptcha") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                addOrReplaceCaptcha();
                // model.setCaptchaText(null);
                target.add(captchaImage);
                target.add(captchaText);
            }
        });
        captchaText = new TextField<String>("captchaText");
        captchaText.setOutputMarkupId(true);
        form.add(captchaText);

        AjaxSubmitLink saveAction = new AjaxSubmitLink("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                save();
             /*
                try {
                    hideModal();
                } catch (ValidationException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    notifierProvider.getNotifier(getPage()).notify("Otra Resolución", ex.getMessageList());
                } catch (Throwable ex) {
                    logger.log(Level.SEVERE, null, ex);
                    notifierProvider.getNotifier(getPage()).notify("Otra Resolución", "Ha ocurrido un error al guardar la Resolución");
                } */
            }
        };
        form.add(saveAction);
    }

    private void save() {
        UserRegistrationData data = form.getModelObject();
       /* OtraResolucionCooperativas otraResolucionCooperativas = new OtraResolucionCooperativas();
        data.copyToEntity(otraResolucionCooperativas);
        if (editing) {
            cooperativasLocalService.updateOtraResolucionCooperativas(otraResolucionCooperativas);
        } else {
            cooperativasLocalService.createOtraResolucionCooperativas(entidadCooperativas.getId(), otraResolucionCooperativas);
        }

        if (callbacks != null) {
            callbacks.onSave();
        } */
    }

    private void addOrReplaceCaptcha() {
        captchaPassword = generateRandomCaptchaString(CAPTCHA_MIN_LENGTH, CAPTCHA_MAX_LENGTH);
        CaptchaImageResource captchaImageResource = new CaptchaImageResource(captchaPassword);
        captchaImage = new NonCachingImage("captchaImage", captchaImageResource);
        captchaImage.setOutputMarkupId(true);
        form.addOrReplace(captchaImage);
    }

}
