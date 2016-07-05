package portal;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.cdi.ConversationPropagation;
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
