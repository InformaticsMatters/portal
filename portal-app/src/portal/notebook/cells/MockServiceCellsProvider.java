package portal.notebook.cells;

import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.types.MoleculeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;

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
                    null, // resource URL e.g. wiki page taht describes the service
                    new String[]{"path/to/find", "road/to/noware"}, // path for location in folders. Do we need this?
                    "User1 <user1@squonk.it>", // owner
                    null, // owner's URL
                    new String[]{"public"}, // layers it appears on
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    Metadata.Type.STREAM, // inputTypes
                    Metadata.Type.STREAM, // outputTypes
                    new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "http://www.somewhere.com/logp", // endpoint
                                    false, // URL is relative
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    null, null, null, null, null, null)
                    }
            ),
            new ServiceDescriptor(
                    "mock.clustering",
                    "Mock clustering",
                    "Mock clustering with 2 integer options",
                    new String[]{"tag2", "tag4", "tag6"},
                    null,
                    new String[]{"path/to/find", "road/to/noware"},
                    "User1 <user1@squonk.it>",
                    null,
                    new String[]{"public"},
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    Metadata.Type.STREAM, // inputTypes
                    Metadata.Type.STREAM, // outputTypes
                    new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "http://www.somewhere.com/clustering", // endpoint
                                    false, // URL is relative
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    null, null, null, null, new OptionDescriptor[]{
                                    new OptionDescriptor<>(Integer.class, "header.min_clusters", "Min clusters", "Minimum number of clusters to generate").withDefaultValue(5),
                                    new OptionDescriptor<>(Integer.class, "header.max_clusters", "Max clusters", "Maximum number of clusters to generate").withDefaultValue(10)
                            }, null)
                    }
            ),
            new ServiceDescriptor(
                    "mock.screening",
                    "Mock screening",
                    "Mock screening with molecule and float and pick list options",
                    new String[]{"tag1", "tag3", "tag5"},
                    null,
                    new String[]{"path/to/find", "road/to/noware"},
                    "User1 <user1@squonk.it>",
                    null,
                    new String[]{"public"},
                    MoleculeObject.class, // inputClass
                    MoleculeObject.class, // outputClass
                    Metadata.Type.STREAM, // inputTypes
                    Metadata.Type.STREAM, // outputTypes
                    new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "http://www.somewhere.com/screening", // endpoint
                                    false, // URL is relative
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    null, null, null, null, new OptionDescriptor[]{
                                    new OptionDescriptor<>(new MoleculeTypeDescriptor<>(MoleculeTypeDescriptor.MoleculeType.DISCRETE), "header.query_structure", "Query Structure", "Structure to us as the query"),
                                    new OptionDescriptor<>(Float.class, "header.threshold", "Similarity Cuttoff", "Similarity score cuttoff between 0 and 1 (1 means identical)")
                                            .withDefaultValue(0.7f),
                                    new OptionDescriptor<>(String.class, "header.descriptor", "descriptor", "Molecular descriptor")
                                            .withValues(new String[]{"Chemical hashed fingerprint", "ECFP4"})
                                            .withDefaultValue("ECFP4")
                            }, null)
                    }
            )
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
        OptionDescriptor[] parameters = serviceDescriptor.getAccessModes()[0].getParameters();
        if (parameters != null) {
            logger.info(parameters.length + " parameters found for service " + serviceDescriptor.getName());
            for (OptionDescriptor parameter : parameters) {
                logger.info("property type: " + parameter.getTypeDescriptor().getType());
                result.getOptionDefinitionList().add(parameter);
            }
        }

        return result;
    }
}
