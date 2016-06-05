package portal.notebook.api;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetDockerProcessorCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "Dataset Docker processor";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(DatasetDockerProcessorCellDefinition.class.getName());
    private static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_IMAGE;
    private static final String OPTION_DOCKER_COMMAND = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND;
    private static final String OPTION_MEDIA_TYPE_INPUT = "inputMediaType";
    private static final String OPTION_MEDIA_TYPE_OUTPUT = "outputMediaType";

    public DatasetDockerProcessorCellDefinition() {
        super(CELL_NAME, "Process dataset using Docker container", "icons/program.png", new String[]{"program", "code", "dataset", "docker"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPTION_DOCKER_IMAGE,
                "Docker image name", "The name of the Docker image to use").withMaxValues(1).withMaxValues(1));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPTION_MEDIA_TYPE_INPUT,
                "Input media type", "The format the input will be written as e.g. application/x-squonk-dataset-molecule+json").withMaxValues(1).withMaxValues(1));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPTION_MEDIA_TYPE_OUTPUT,
                "Output media type", "The format the output will be read as e.g. chemical/x-mdl-sdfile").withMaxValues(1).withMaxValues(1));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPTION_DOCKER_COMMAND,
                "Command", "The command that will be executed e.g. to execute bash script inside container e.g. chemical/x-mdl-sdfile").withMaxValues(1).withMaxValues(1));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.DockerProcessDataset.CLASSNAME);
    }

}
