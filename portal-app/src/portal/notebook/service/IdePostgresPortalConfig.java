package portal.notebook.service;

import java.util.Properties;

public class IdePostgresPortalConfig implements PortalConfig {

    @Override
    public Properties getPersistenceProperties() {

        Properties properties = new Properties();
        properties.setProperty("javax.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/notebooks");
        properties.setProperty("javax.persistence.jdbc.password", "admin");
        properties.setProperty("javax.persistence.jdbc.driver", "org.postgresql.Driver");
        properties.setProperty("javax.persistence.jdbc.user", "admin");
        properties.setProperty("eclipselink.ddl-generation", "create-tables");
        //prop.setProperty("eclipselink.logging.level", "FINE");
        return properties;
    }
}
