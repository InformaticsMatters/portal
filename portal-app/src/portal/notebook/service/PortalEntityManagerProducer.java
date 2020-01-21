package portal.notebook.service;

import toolkit.services.PU;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

import org.squonk.util.IOUtils;

@ApplicationScoped
public class PortalEntityManagerProducer {

    @Inject
    private PortalConfig config;
    private static EntityManagerFactory emf;

    private void checkEMF() {
        if (emf == null) {
            Properties properties = config.getPersistenceProperties();
            if (properties == null) {
                emf = Persistence.createEntityManagerFactory(PortalConstants.PU_NAME);
            } else {
                String hostname = IOUtils.getConfiguration("POSTGRES_HOSTNAME", null);
                if (hostname != null) {
                    String database = IOUtils.getConfiguration("POSTGRES_SQUONK_DATABASE", "squonk");
                    properties.put("javax.persistence.jdbc.url", "jdbc:postgresql://" + hostname + "/" + database);
                }
                String pw = IOUtils.getConfiguration("POSTGRES_SQUONK_PASSWORD", null);
                if (pw != null) {
                    properties.put("javax.persistence.jdbc.password", pw);
                }
                String usr = IOUtils.getConfiguration("POSTGRES_SQUONK_USER", null);
                if (usr != null) {
                    properties.put("javax.persistence.jdbc.user", usr);
                }
                emf = Persistence.createEntityManagerFactory(PortalConstants.PU_NAME, properties);
            }
        }
    }

    @Produces
    @PU(puName = PortalConstants.PU_NAME)
    @RequestScoped
    EntityManager createEntityManager() {
        checkEMF();
        return emf.createEntityManager();
    }

    void close(@Disposes @PU(puName = PortalConstants.PU_NAME) EntityManager entityManager) {
        entityManager.close();
    }
}
