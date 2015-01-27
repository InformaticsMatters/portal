package portal.service.datamart;

import portal.service.api.StructureSearch;
import toolkit.test.AbstractTestCase;
import toolkit.test.TestCase;
import toolkit.test.TestMethod;

/**
 * @author simetrias
 */
@TestCase
public class DatamartServiceTest extends AbstractTestCase {

    public static void main(String[] args) throws Exception {
        runTestCase(DatamartServiceTest.class);
    }

    @TestMethod
    public void test() {
        DatamartService datamartService = new DatamartService();
        datamartService.createFromStructureSearch(new StructureSearch());
    }
}
