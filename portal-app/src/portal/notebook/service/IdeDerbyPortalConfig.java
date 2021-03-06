package portal.notebook.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.Properties;

@Alternative
@ApplicationScoped
public class IdeDerbyPortalConfig implements PortalConfig {

    @Override
    public Properties getPersistenceProperties() {

        Properties properties = new Properties();
        properties.setProperty("javax.persistence.jdbc.url", "jdbc:derby://localhost:1527/portal;create=true");
        properties.setProperty("javax.persistence.jdbc.password", "admin");
        properties.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.ClientDriver");
        properties.setProperty("javax.persistence.jdbc.user", "admin");
        properties.setProperty("eclipselink.ddl-generation", "create-tables");
        //prop.setProperty("eclipselink.logging.level", "FINE");
        return properties;
    }
}
