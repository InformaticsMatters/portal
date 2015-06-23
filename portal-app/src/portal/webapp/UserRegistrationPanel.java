package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

public class UserRegistrationPanel extends Panel {

    private static final Integer CAPTCHA_MIN_LENGTH = 6;
    private static final Integer CAPTCHA_MAX_LENGTH = 8;

    private Form<UserRegistrationData> form;
    private String captchaPassword;
    private NonCachingImage captchaImage;
    private TextField<String> captchaText;

    public UserRegistrationPanel(String id) {
        super(id);
        addForm();
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
                System.out.println("Saving user...");
            }
        };
        form.add(saveAction);
    }

    private void addOrReplaceCaptcha() {
        captchaPassword = generateRandomCaptchaString(CAPTCHA_MIN_LENGTH, CAPTCHA_MAX_LENGTH);
        CaptchaImageResource captchaImageResource = new CaptchaImageResource(captchaPassword);
        captchaImage = new NonCachingImage("captchaImage", captchaImageResource);
        captchaImage.setOutputMarkupId(true);
        form.addOrReplace(captchaImage);
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

}
