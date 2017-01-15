package portal.notebook.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.core.ServiceConfig;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.FieldActionTypeDescriptor;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;

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

    /*
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            Class inputClass,
            Class outputClass,
            IODescriptor.DataType inputType,
            IODescriptor.DataType outputType,
            OptionDescriptor[] options,
            ServiceConfig.Status status,
            Date statusLastChecked
            String executorClassName
     */

    public static final ServiceConfig[] MOCK_SERVICE_DESCRIPTORS = new ServiceConfig[]{
            new ServiceConfig(
                    "mock.nooptions", // key
                    "Simple no options name", // name
                    "Simple no options desc", // description
                    new String[]{"tag1", "tag2", "tag3"}, // tags for searching
                    "http://some.where/docs", // resource URL e.g. wiki page that describes the service
                    "default_icon.png",
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("input")},
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")},
                    null, // options
                    null, null, // status
                    "executor.class.Name"
            ),
            new ServiceConfig(
                    "mock.clustering",
                    "Mock clustering",
                    "Mock clustering with 2 integer options",
                    new String[]{"tag2", "tag4", "tag6"},
                    "http://some.where/docs",
                    "default_icon.png",
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("input")},
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")},
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(Integer.class, "header.min_clusters", "Min clusters", "Minimum number of clusters to generate", Mode.User).withDefaultValue(5),
                            new OptionDescriptor<>(Integer.class, "header.max_clusters", "Max clusters", "Maximum number of clusters to generate", Mode.User).withDefaultValue(10)
                    },
                    null, null, // status
                    "executor.class.Name"),
            new ServiceConfig(
                    "mock.screening",
                    "Mock screening",
                    "Mock screening with molecule and float and pick list options",
                    new String[]{"tag1", "tag3", "tag5"},
                    "http://some.where/docs",
                    "default_icon.png",
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("input")},
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")},
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.DISCRETE, new String[]{"smiles"}),
                                    "header.query_structure", "Query Structure", "Structure to us as the query", Mode.User),
                            new OptionDescriptor<>(Float.class, "header.threshold", "Similarity Cuttoff", "Similarity score cuttoff between 0 and 1 (1 means identical)", Mode.User)
                                    .withDefaultValue(0.7f),
                            new OptionDescriptor<>(String.class, "header.descriptor", "descriptor", "Molecular descriptor", Mode.User)
                                    .withValues(new String[]{"Chemical hashed fingerprint", "ECFP4"})
                                    .withDefaultValue("ECFP4")
                    },
                    null, null, // status
                    "executor.class.Name"
            ),
            new ServiceConfig(
                    "mock.deduplication",
                    "Mock deduplication",
                    "Mock deduplication with filed pick list options",
                    new String[]{"tag1", "tag3", "tag5"},
                    "http://some.where/docs",
                    "default_icon.png",
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("input")},
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")},
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
                    },
                    null, null, // status
                    "executor.class.Name"
            )
    };
    private static final Logger logger = LoggerFactory.getLogger(MockServiceCellsProvider.class);

    @Override
    public List<ServiceCellDefinition> listServiceCellDefinition() {
        List<ServiceCellDefinition> result = new ArrayList<>();
        for (ServiceConfig serviceConfig : MOCK_SERVICE_DESCRIPTORS) {
            result.add(buildCellDefinitionForServiceDescriptor(serviceConfig));
        }
        return result;
    }

    private ServiceCellDefinition buildCellDefinitionForServiceDescriptor(ServiceConfig serviceConfig) {
        ServiceCellDefinition result = new ServiceCellDefinition(serviceConfig);
        return result;
    }
}
