package portal.notebook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author simetrias
 */
@Default
@ApplicationScoped
public class PropertiesFileNotebookConfig implements NotebookConfig {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesFileNotebookConfig.class);
    private Properties persistenceProperties;
    @Inject
    private ServletContext servletContext;

    @Override
    public Properties getPersistenceProperties() {
        return persistenceProperties;
    }

    @PostConstruct
    public void loadConfig() {
        persistenceProperties = new Properties();
        String path = servletContext.getRealPath("/WEB-INF/persistence.properties");
        boolean loaded = false;
        if (path != null) {
            File propertiesFile = new File(path);
            logger.info("Looking for config file at " + propertiesFile.getAbsolutePath());

            if (propertiesFile.exists()) {
                loadPersistencePropertiesFile(propertiesFile, persistenceProperties);
                loaded = true;
            }
        }
        if (!loaded) {
            loadDefaultPersistenceProperties(persistenceProperties);
        }
        logger.info("Using database " + persistenceProperties.getProperty("javax.persistence.jdbc.url"));
    }

    private void loadDefaultPersistenceProperties(Properties properties) {
        properties.setProperty("javax.persistence.jdbc.url", "jdbc:derby://localhost:1527/notebooks;create=true");
        properties.setProperty("javax.persistence.jdbc.password", "admin");
        properties.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.ClientDriver");
        properties.setProperty("javax.persistence.jdbc.user", "admin");
        properties.setProperty("eclipselink.ddl-generation", "create-tables");
    }

    private void loadPersistencePropertiesFile(File file, Properties properties) {
        try {
            try (InputStream inputStream = new FileInputStream(file)) {
                properties.load(inputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

