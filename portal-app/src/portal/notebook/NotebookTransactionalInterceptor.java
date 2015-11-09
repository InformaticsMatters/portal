package portal.notebook;

import portal.notebook.service.NotebookConstants;
import toolkit.services.PU;
import toolkit.services.Transactional;
import toolkit.services.TransactionalInterceptor;

import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;
import java.io.Serializable;

@Interceptor
@Transactional
public class NotebookTransactionalInterceptor extends TransactionalInterceptor implements Serializable {
    @Inject
    @PU(puName = NotebookConstants.PU_NAME)
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
