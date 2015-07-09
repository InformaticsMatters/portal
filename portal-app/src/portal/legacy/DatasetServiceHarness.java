package portal.legacy;

import portal.dataset.DatasetDescriptor;
import portal.dataset.DatasetService;
import portal.service.api.StructureSearch;
import toolkit.test.AbstractTestCase;
import toolkit.test.TestCase;
import toolkit.test.TestMethod;

import javax.inject.Inject;

/**
 * @author simetrias
 */
@TestCase
public class DatasetServiceHarness extends AbstractTestCase {

    @Inject
    private DatasetService service;

    public static void main(String[] args) throws Exception {
        runTestCase(DatasetServiceHarness.class);
    }

    // @TestMethod
    public void testStructureSearch() {
        StructureSearch structureSearch = new StructureSearch();
        structureSearch.setStructure("CN1C=NC2=C1C(=O)N(C)C(=O)N2C");
        service.createFromStructureSearch(structureSearch);
    }

    @TestMethod
    public void testDatasetService() {
        DatasetDescriptorMock datasetDescriptorMock = new DatasetDescriptorMock();
        datasetDescriptorMock.setDescription("Test dataset descriptor");
        DatasetDescriptor result = service.createDatasetDescriptor(datasetDescriptorMock);
    }
}
