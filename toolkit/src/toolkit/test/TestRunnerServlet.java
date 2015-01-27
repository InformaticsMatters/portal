package toolkit.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author simetrias
 */
public class TestRunnerServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(TestRunnerServlet.class.getName());
    private static final String BEAN_MANAGER = "org.jboss.weld.environment.servlet.javax.enterprise.inject.spi.BeanManager";
    @Inject
    private TestContext testContext;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            String testClassName = request.getParameter("testClass");
            if (testClassName == null) {
                String[] testPackageNames = buildTestPackageNames();
                processBeans(testPackageNames);
            } else {
                processClass(Class.forName(testClassName));
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private String[] buildTestPackageNames() {
        List<String> names = new ArrayList<String>();
        String property = System.getProperty("test.packages", "");
        String[] split = property.split(";");
        for (int i = 0; i < split.length; i++) {
            String name = split[i].trim();
            if (name.length() > 0) {
                names.add(name);
            }
        }
        return names.toArray(new String[names.size()]);
    }

    private void processBeans(String[] testPackageNames) throws IOException, Exception {
        BeanManager beanManager = (BeanManager) getServletContext().getAttribute(BEAN_MANAGER);
        testContext.setBeanManager(beanManager);
        Set<Bean<?>> beans = beanManager.getBeans(Object.class, new AnnotationLiteral<TestCase>() {
        });
        for (Bean<?> bean : beans) {
            if (!isTestPackageName(testPackageNames, bean.getBeanClass().getPackage().getName())) {
                continue;
            }
            processBean(bean, beanManager);
        }

    }

    private void processClass(Class testClass) throws Exception {
        BeanManager beanManager = (BeanManager) getServletContext().getAttribute(BEAN_MANAGER);
        testContext.setBeanManager(beanManager);
        Set<Bean<?>> beans = beanManager.getBeans(testClass, new AnnotationLiteral<TestCase>() {
        });
        processBean(beans.iterator().next(), beanManager);
    }

    private void processBean(Bean<?> bean, BeanManager beanManager) throws Exception {
        CreationalContext creationalContext = beanManager.createCreationalContext(bean);
        Object object = beanManager.getReference(bean, bean.getBeanClass(), creationalContext);
        processObject(bean.getBeanClass(), object);
    }

    private boolean isTestPackageName(String[] testPackageNames, String packageName) {
        for (String testPackageName : testPackageNames) {
            if (packageName.startsWith(testPackageName)) {
                return true;
            }
        }
        return false;
    }

    private void processObject(Class objectClass, Object object) throws Exception {
        Method[] methods = objectClass.getDeclaredMethods();
        List<Method> testMethods = new ArrayList<Method>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestMethod.class)) {
                testMethods.add(method);
            }
        }
        Collections.sort(testMethods, new Comparator<Method>() {
            @Override
            public int compare(Method method1, Method method2) {
                TestMethod annotation1 = method1.getAnnotation(TestMethod.class);
                TestMethod annotation2 = method2.getAnnotation(TestMethod.class);
                int ordinal1 = annotation1.ordinal();
                int ordinal2 = annotation2.ordinal();
                if (ordinal1 < 0 && ordinal2 >= 0) {
                    return 1;
                } else if (ordinal2 < 0 && ordinal1 >= 0) {
                    return -1;
                } else {
                    return ordinal1 - ordinal2;
                }
            }
        });
        for (Method method : testMethods) {
            try {
                logger.info("Running {0}:{1}", new Object[]{objectClass.getName(), method.getName()});
                method.invoke(object, new Object[0]);
            } catch (InvocationTargetException ex) {
                logger.error(objectClass.getName() + ":" + method.getName(), ex.getCause());
            }
        }
    }

}
