package portal.notebook.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptor.DataType;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.FieldActionTypeDescriptor;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.MoleculeObject;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
@ApplicationScoped
@Alternative
public class MockServiceCellsProvider implements ServiceCellsProvider {

    public static final ServiceDescriptor[] MOCK_SERVICE_DESCRIPTORS = new ServiceDescriptor[]{
            new ServiceDescriptor(
                    "mock.nooptions", // key
                    "Simple no options", // name
                    "Simple no options", // description
                    new String[]{"tag1", "tag2", "tag3"}, // tags for searching
                    null, // resource URL e.g. wiki page that describes the service
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    DataType.STREAM, // inputTypes
                    DataType.STREAM, // outputTypes
                    "default_icon.png",
                    "http://www.somewhere.com/logp", // endpoint
                    false, // URL is relative
                    null, null),
            new ServiceDescriptor(
                    "mock.clustering",
                    "Mock clustering",
                    "Mock clustering with 2 integer options",
                    new String[]{"tag2", "tag4", "tag6"},
                    null,
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    DataType.STREAM, // inputTypes
                    DataType.STREAM, // outputTypes
                    "default_icon.png",
                    "http://www.somewhere.com/clustering", // endpoint
                    false, // URL is relative
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(Integer.class, "header.min_clusters", "Min clusters", "Minimum number of clusters to generate", Mode.User).withDefaultValue(5),
                            new OptionDescriptor<>(Integer.class, "header.max_clusters", "Max clusters", "Maximum number of clusters to generate", Mode.User).withDefaultValue(10)
                    }, null),
            new ServiceDescriptor(
                    "mock.screening",
                    "Mock screening",
                    "Mock screening with molecule and float and pick list options",
                    new String[]{"tag1", "tag3", "tag5"},
                    null,
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    DataType.STREAM, // inputTypes
                    DataType.STREAM, // outputTypes
                    "default_icon.png",
                    "http://www.somewhere.com/screening", // endpoint
                    false, // URL is relative
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.DISCRETE, new String[]{"smiles"}),
                                    "header.query_structure", "Query Structure", "Structure to us as the query", Mode.User),
                            new OptionDescriptor<>(Float.class, "header.threshold", "Similarity Cuttoff", "Similarity score cuttoff between 0 and 1 (1 means identical)", Mode.User)
                                    .withDefaultValue(0.7f),
                            new OptionDescriptor<>(String.class, "header.descriptor", "descriptor", "Molecular descriptor", Mode.User)
                                    .withValues(new String[]{"Chemical hashed fingerprint", "ECFP4"})
                                    .withDefaultValue("ECFP4")
                    }, null),
            new ServiceDescriptor(
                    "mock.deduplication",
                    "Mock deduplication",
                    "Mock deduplication with filed pick list options",
                    new String[]{"tag1", "tag3", "tag5"},
                    null,
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    DataType.STREAM, // inputTypes
                    DataType.STREAM, // outputTypes
                    "default_icon.png",
                    "http://www.somewhere.com/screening", // endpoint
                    false, // URL is relative
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(new DatasetFieldTypeDescriptor(new Class[]{String.class}),
                                    "cansmiles", "Canonical smiles field", "File with canonical smiles that identifies identical structures", Mode.User),
                            new OptionDescriptor<>(new DatasetFieldTypeDescriptor(),
                                    "keepfirstFields", "Keep first value fields", "When multiple values keep the first fields", Mode.User)
                                    .withMinValues(0),
                            new OptionDescriptor<>(new DatasetFieldTypeDescriptor(),
                                    "keeplastFields", "Keep last value fields", "When multiple values keep the last fields", Mode.User)
                                    .withMinValues(0),
                            new OptionDescriptor<>(new DatasetFieldTypeDescriptor(),
                                    "appendFields", "Append value fields", "When multiple values append to list fields", Mode.User)
                                    .withMinValues(0),
                            new OptionDescriptor<>(new FieldActionTypeDescriptor(new String[]{"First", "Last", "Min", "Max", "Append", "Distinct"}),
                                    "fieldHandling", "Field handling", "How to handle the individual fields", Mode.User)
                                    .withMinMaxValues(1, 1)

                    }, null)
    };
    private static final Logger logger = LoggerFactory.getLogger(MockServiceCellsProvider.class);

    @Override
    public List<ServiceCellDefinition> listServiceCellDefinition() {
        List<ServiceCellDefinition> result = new ArrayList<>();
        for (ServiceDescriptor serviceDescriptor : MOCK_SERVICE_DESCRIPTORS) {
            result.add(buildCellDefinitionForServiceDescriptor(serviceDescriptor));
        }
        return result;
    }

    private ServiceCellDefinition buildCellDefinitionForServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        ServiceCellDefinition result = new ServiceCellDefinition(serviceDescriptor);
        OptionDescriptor[] options = serviceDescriptor.getOptions();
        if (options != null) {
            logger.info(options.length + " parameters found for service " + serviceDescriptor.getName());
            for (OptionDescriptor option : options) {
                logger.info("property type: " + option.getTypeDescriptor().getType());
                result.getOptionDefinitionList().add(option);
            }
        }

        return result;
    }
}
