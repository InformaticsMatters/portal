package portal.webapp;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.cdi.ConversationPropagation;
import org.apache.wicket.protocol.http.WebApplication;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public class PortalWebApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return PortalHomePage.class;
    }

    @Override
    protected void init() {
        super.init();
        BeanManager beanManager = CDI.current().getBeanManager();
        new CdiConfiguration(beanManager).setPropagation(ConversationPropagation.NONE).configure(this);

        getSharedResources().add("structureImageResource", new DynamicStructureImageResource());

        mountPage("/login", LoginPage.class);
        mountPage("/userCreated", UserCreatedPage.class);
        mountPage("/emailConfirmation", ConfirmationEmailPage.class);
        mountPage("/forgotPassword", ForgotPasswordPage.class);
        mountPage("/portal", DashboardPage.class);

    }

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        return RuntimeConfigurationType.DEVELOPMENT;
    }
}
