package portal.webapp.notebook;

import toolkit.services.PU;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

public class NotebooksEntityManagerProducer {

    @Inject
    private NotebooksConfig config;
    private static EntityManagerFactory emf;

    private void checkEMF() {
        if (emf == null) {
            Properties properties = config.getPersistenceProperties();
            if (properties == null) {
                emf = Persistence.createEntityManagerFactory(NotebookConstants.PU_NAME);
            } else {
                emf = Persistence.createEntityManagerFactory(NotebookConstants.PU_NAME, properties);
            }
        }
    }

    @Produces
    @PU(puName = NotebookConstants.PU_NAME)
    @RequestScoped
    EntityManager createEntityManager() {
        checkEMF();
        return emf.createEntityManager();
    }

    void close(@Disposes @PU(puName = NotebookConstants.PU_NAME) EntityManager entityManager) {
        entityManager.close();
    }
}
