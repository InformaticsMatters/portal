package portal.notebook.service;

import toolkit.services.PU;
import toolkit.services.ServiceSecurityContext;
import toolkit.services.Transactional;
import toolkit.services.TransactionalInterceptor;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@Interceptor
@Transactional
public class PortalTransactionalInterceptor extends TransactionalInterceptor implements Serializable {

    @Inject
    @PU(puName = PortalConstants.PU_NAME)
    private EntityManager entityManager;
    @Inject
    private ServiceSecurityContext serviceSecurityContext;
    @Inject
    private HttpServletRequest httpServletRequest;


    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    @AroundInvoke
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        serviceSecurityContext.loadSecurityHeadersFromRequest(httpServletRequest);
        return super.aroundInvoke(ic);
    }
}
