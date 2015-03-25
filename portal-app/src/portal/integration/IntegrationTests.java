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

    // @TestMethod
    public void testListPropertyDefinitions() {
        List<PropertyDefinition> result = client.listPropertyDefinition("adenosine", 20);
        for (PropertyDefinition propertyDefinition : result) {
            System.out.println(propertyDefinition.getId());
        }
    }

    // @TestMethod
    public void testListStructuresByHitlist() {
        List<Structure> result = client.listStructure(2l);
        for (Structure structure : result) {
            System.out.println(structure.getCdFormula());
        }
    }

    @TestMethod
    public void testListHitlist() {
        List<Hitlist> result = client.listHitlist();
        for (Hitlist hitlist : result) {
            System.out.println(hitlist.getName() + " - " + hitlist.getCreated());
        }
    }

    @TestMethod
    public void testListPropertyData() {
        List<PropertyData> result = client.listPropertyData(2l, "CHEMBL1614027");
        for (PropertyData propertyData : result) {
            System.out.println(propertyData.getData());
        }
    }
}
