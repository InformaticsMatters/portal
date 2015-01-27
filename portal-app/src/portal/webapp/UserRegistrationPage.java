package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class UserRegistrationPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    private Form<UserRegistrationData> form;

    public UserRegistrationPage() {
        notifierProvider.createNotifier(this, "notifier");
        addForm();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/semantic-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/easygrid-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalHomePage.class, "resources/lac.css")));
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

}
