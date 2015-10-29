package portal;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.cdi.ConversationPropagation;
import org.apache.wicket.protocol.http.WebApplication;
import portal.notebook.NotebookCanvasPage;
import portal.notebook.NotebookStructureImageResource;
import portal.visualizers.DynamicStructureImageResource;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

public class PortalWebApplication extends WebApplication {

    @Inject
    private ComponentInstantiationListener componentInstantiationListener;

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
        getSharedResources().add("notebookStructureImageResource", new NotebookStructureImageResource());
        mountPage("/nbcanvas", NotebookCanvasPage.class);
        configureSecurity();
    }

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        return RuntimeConfigurationType.DEVELOPMENT;
    }

    protected void configureSecurity() {
        getComponentInstantiationListeners().add(componentInstantiationListener);
    }
}
