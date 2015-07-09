package portal.chemcentral;

import toolkit.services.PU;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

/**
 * @author simetrias
 */
public class ChemcentralEntityManagerProducer {

    public static final String CHEMCENTRAL_PU_NAME = "chemcentral";
    private static EntityManagerFactory chemcentralEMF;
    @Inject
    private ChemcentralConfig chemcentralConfig;

    private void checkEMF() {
        if (chemcentralEMF == null) {
            Properties properties = chemcentralConfig.getChemcentralPersistenceProperties();
            if (properties == null) {
                chemcentralEMF = Persistence.createEntityManagerFactory(CHEMCENTRAL_PU_NAME);
            } else {
                chemcentralEMF = Persistence.createEntityManagerFactory(CHEMCENTRAL_PU_NAME, properties);
            }
        }
    }

    @Produces
    @PU(puName = CHEMCENTRAL_PU_NAME)
    @RequestScoped
    EntityManager createEntityManager() {
        checkEMF();
        return chemcentralEMF.createEntityManager();
    }

    void close(@Disposes @PU(puName = CHEMCENTRAL_PU_NAME) EntityManager entityManager) {
        entityManager.close();
    }
}
