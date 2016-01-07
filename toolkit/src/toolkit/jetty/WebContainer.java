package toolkit.jetty;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author simetrias
 */
public class WebContainer {

    private static final Logger logger = LoggerFactory.getLogger(WebContainer.class.getName());
    private static final Properties properties;

    private static final String WEBCONTAINER_PORT = "webcontainer_port";
    private static final String WEBCONTAINER_WEBAPP = "webcontainer_webapp";

    private static final String DEFAULT_WEBAPP = "webapp";
    private static final String DEFAULT_PORT = "8080";

    static {
        properties = new Properties();
        try {
            File propFile = new File("webservices.properties");
            if (propFile.exists()) {
                properties.load(new FileReader(propFile));
            }
        } catch (IOException e) {
            logger.error(null, e);
        }
    }

    public static void main(String[] args) {
        try {
            WebContainer server = new WebContainer();
            server.start();
        } catch (Throwable t) {
            logger.error(null, t);
            System.exit(1);
        }
    }

    private void startWebServer() throws Exception {
        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        int port = new Integer(properties.getProperty(WEBCONTAINER_PORT, DEFAULT_PORT));
        connector.setPort(port);
        server.addConnector(connector);
        String webapp = properties.getProperty(WEBCONTAINER_WEBAPP, DEFAULT_WEBAPP);
        WebAppContext wac = new WebAppContext(webapp, "/");

        HashLoginService loginService = new HashLoginService();
        loginService.setName("lac");
        loginService.setConfig("realm.properties");

        loginService.putUser("user1", Credential.getCredential("user1"), new String[]{"user"});
        loginService.putUser("user2", Credential.getCredential("user2"), new String[]{"user"});

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.setRealmName("lac");

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.NONE);
        constraint.setAuthenticate(false);
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/ws/*");
        securityHandler.addConstraintMapping(constraintMapping);

        constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"user"});
        constraint.setAuthenticate(true);

        constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");
        securityHandler.addConstraintMapping(constraintMapping);

        securityHandler.setLoginService(loginService);


        wac.setSecurityHandler(securityHandler);
        server.setHandler(wac);
        server.setStopAtShutdown(true);
        server.start();
    }

    public void start() throws Exception {
        startWebServer();
    }

}
