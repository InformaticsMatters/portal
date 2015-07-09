package portal.chemcentral;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.Properties;

@Alternative
@ApplicationScoped
public class ChemcentralIDEAConfig implements ChemcentralConfig {

    @Override
    public Properties getChemcentralPersistenceProperties() {
        Properties prop = new Properties();
        prop.setProperty("javax.persistence.jdbc.url", "jdbc:postgresql://demos.informaticsmatters.com:49153/chemcentral");
        prop.setProperty("javax.persistence.jdbc.user", "postgres");
        prop.setProperty("javax.persistence.jdbc.password", "8TMEoc8x");
        prop.setProperty("javax.persistence.jdbc.driver", "org.postgresql.Driver");
        prop.setProperty("eclipselink.ddl-generation", "create-tables");
        prop.setProperty("eclipselink.logging.level.sql", "FINE");
        prop.setProperty("eclipselink.logging.parameters", "true");
        return prop;
    }

}
