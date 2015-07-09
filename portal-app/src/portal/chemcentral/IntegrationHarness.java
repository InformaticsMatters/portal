package portal.chemcentral;

import toolkit.test.AbstractTestCase;
import toolkit.test.TestCase;
import toolkit.test.TestMethod;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author simetrias
 */
@TestCase
public class IntegrationHarness extends AbstractTestCase {

    @Inject
    private ChemcentralClient client;

    public static void main(String[] args) throws Exception {
        runTestCase(IntegrationHarness.class);
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

    // @TestMethod
    public void testCreateHitListWithDataFor() {
        String uri = client.createHitListWithDataFor(2, Arrays.asList("CHEMBL1613886"));
        System.out.println(uri);
    }

    @TestMethod
    public void testListHitlist() {
        List<Hitlist> result = client.listHitlist();
        for (Hitlist hitlist : result) {
            System.out.println(hitlist.getName() + " - " + hitlist.getCreated() + " - " + hitlist.getStatus());
        }
    }

    // @TestMethod
    public void testListPropertyData() {
        List<PropertyData> result = client.listPropertyData(2l, "CHEMBL1614027");
        for (PropertyData propertyData : result) {
            System.out.println(propertyData.getData());
        }
    }
}
