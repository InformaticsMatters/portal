package portal;

import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentInstantiationListener;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class ComponentInstantiationListener implements IComponentInstantiationListener {

    @Inject
    private SessionContext sessionContext;

    @Override
    public void onInstantiation(Component component) {
    }

}
