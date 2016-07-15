package portal;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.cdi.ConversationPropagation;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;
import portal.notebook.webapp.NotebookCanvasPage;
import portal.notebook.webapp.NotebookStructureImageResource;
import toolkit.derby.DerbyUtils;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public class PortalWebApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return NotebookCanvasPage.class;
    }

    @Override
    protected void init() {
        super.init();
        checkDerbyServer();
        allowExtraPatterns();
        BeanManager beanManager = CDI.current().getBeanManager();
        new CdiConfiguration(beanManager).setPropagation(ConversationPropagation.NONE).configure(this);
        getSharedResources().add("notebookStructureImageResource", new NotebookStructureImageResource());
    }

    private void checkDerbyServer() {
        try {
            if (System.getProperty("startDerbyServer", "false").equals("true")) {
                DerbyUtils.startDerbyServer();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void allowExtraPatterns() {
        IPackageResourceGuard packageResourceGuard = getResourceSettings().getPackageResourceGuard();
        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
            guard.addPattern("+*.woff2");
        }
    }

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        RuntimeConfigurationType result = RuntimeConfigurationType.DEPLOYMENT;
        if (isDevelopmentEnvironment()) {
            result = RuntimeConfigurationType.DEVELOPMENT;
        }
        return result;
    }

    private boolean isDevelopmentEnvironment() {
        String development = System.getProperty("development");
        return Boolean.parseBoolean(development);
    }
}
