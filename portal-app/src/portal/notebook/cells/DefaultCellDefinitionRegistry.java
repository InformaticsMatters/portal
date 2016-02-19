package portal.notebook.cells;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.core.CommonConstants;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.client.ServicesClient;
import org.squonk.options.OptionDescriptor;
import portal.SessionContext;
import portal.notebook.api.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.*;

@Default
@ApplicationScoped
public class DefaultCellDefinitionRegistry implements CellDefinitionRegistry {

    public static final String VAR_NAME_INPUT = "input";
    public static final String VAR_NAME_OUTPUT = "output";
    public static final String VAR_NAME_FILECONTENT = "fileContent";
    private static final Logger logger = LoggerFactory.getLogger(DefaultCellDefinitionRegistry.class);
    private final Map<String, CellDefinition> cellDefinitionMap = new LinkedHashMap<>();
    @Inject
    private SessionContext sessionContext;

    public DefaultCellDefinitionRegistry() {

        registerCellDefinition(new ChemblActivitiesFetcherCellDefinition());
        registerCellDefinition(createTableDisplayCellDefinition());
        registerCellDefinition(new CsvUploadCellDefinition());
        registerCellDefinition(new SdfUploadCellDefinition());
        registerCellDefinition(new DatasetMergerCellDefinition());
        registerCellDefinition(new ConvertToMoleculesCellDefinition());
        registerCellDefinition(new TransformValuesCellDefinition());
        registerCellDefinition(new ProcessDatasetTrustedGroovyScriptCellDefinition());
    }

    private static CellDefinition createTableDisplayCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition();
        cellDefinition.setName("TableDisplay");
        cellDefinition.setDescription("Table display");
        cellDefinition.setExecutable(Boolean.FALSE);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.VALUE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        return cellDefinition;
    }

    public void registerCellDefinition(CellDefinition cellDefinition) {
        cellDefinitionMap.put(cellDefinition.getName(), cellDefinition);
    }

    public Collection<CellDefinition> listCellDefinition() {
        List<CellDefinition> definitionList = new ArrayList<>();
        definitionList.addAll(cellDefinitionMap.values());
        addServiceCellDefinitionList(definitionList);
        return definitionList;
    }

    public CellDefinition findCellDefinition(String name) {
        return cellDefinitionMap.get(name);
    }

    private void addServiceCellDefinitionList(Collection<CellDefinition> cellDefinitionList) {
        for (ServiceDescriptor serviceDescriptor : listServiceDescriptors()) {
            cellDefinitionList.add(buildCellDefinitionForServiceDescriptor(serviceDescriptor));
        }
    }

    private List<ServiceDescriptor> listServiceDescriptors() {
        ServicesClient servicesClient = new ServicesClient(CommonConstants.HOST_CORE_SERVICES_SERVICES);
        List<ServiceDescriptor> serviceDescriptors;
        try {
            serviceDescriptors = servicesClient.getServiceDescriptors(sessionContext.getLoggedInUserDetails().getUserid());
        } catch (Throwable e) {
            serviceDescriptors = new ArrayList<>();
            logger.error(null, e);
        }
        return serviceDescriptors;
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
