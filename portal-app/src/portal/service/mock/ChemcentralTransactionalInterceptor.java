package portal.service.mock;

import toolkit.services.NamedTransactional;
import toolkit.services.PU;
import toolkit.services.TransactionalInterceptor;

import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;

/**
 * @author simetrias
 */
@Interceptor
@NamedTransactional(puName = ChemcentralEntityManagerProducer.CHEMCENTRAL_PU_NAME)
public class ChemcentralTransactionalInterceptor extends TransactionalInterceptor {

    @Inject
    @PU(puName = ChemcentralEntityManagerProducer.CHEMCENTRAL_PU_NAME)
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
