package portal.integration;

import toolkit.test.AbstractTestCase;
import toolkit.test.TestCase;
import toolkit.test.TestMethod;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
@TestCase
public class IntegrationTests extends AbstractTestCase {

    @Inject
    private IntegrationClient client;

    public static void main(String[] args) throws Exception {
        runTestCase(IntegrationTests.class);
    }

    @TestMethod
    public void testListPropertyDefinitions() {
        List<PropertyDefinition> result = client.propertyDefinitions("adenosine", 20);
        for (PropertyDefinition propertyDefinition : result) {
            System.out.println(propertyDefinition.getPropertyDescription());
        }
    }
}
