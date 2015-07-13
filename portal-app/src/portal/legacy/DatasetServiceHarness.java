package portal.legacy;

import portal.chemcentral.StructureSearch;
import portal.dataset.IDatasetDescriptor;
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
        IDatasetDescriptor result = service.createDatasetDescriptor(datasetDescriptorMock);
    }
}
