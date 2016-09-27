package portal.notebook.api;

import org.squonk.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.util.CommonMimeTypes;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetDockerProcessorCellDefinition extends CellDefinition implements CommonMimeTypes {
    public static final String CELL_NAME = "Dataset Docker processor";
    private final static long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(DatasetDockerProcessorCellDefinition.class.getName());
    private static final String OPTION_DOCKER_IMAGE = StepDefinitionConstants.OPTION_DOCKER_IMAGE;
    private static final String OPTION_DOCKER_COMMAND = StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND;
    private static final String OPTION_MEDIA_TYPE_INPUT = "inputMediaType";
    private static final String OPTION_MEDIA_TYPE_OUTPUT = "outputMediaType";

    public DatasetDockerProcessorCellDefinition() {
        super(CELL_NAME, "Process dataset using Docker container", "icons/program.png", new String[]{"program", "code", "dataset", "docker"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPTION_DOCKER_IMAGE,
                "Docker image name", "The name of the Docker image to use", Mode.User)
                .withMinMaxValues(1,1));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPTION_MEDIA_TYPE_INPUT,
                "Input media type", "The format the input will be written as e.g. application/x-squonk-dataset-molecule+json", Mode.User)
                .withValues(new String[] {CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, CommonMimeTypes.MIME_TYPE_MDL_SDF})
                .withDefaultValue(CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withMinMaxValues(1,1));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPTION_MEDIA_TYPE_OUTPUT,
                "Output media type", "The format the output will be read as e.g. chemical/x-mdl-sdfile", Mode.User)
                .withValues(new String[] {CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, CommonMimeTypes.MIME_TYPE_MDL_SDF})
                .withDefaultValue(CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                .withMinMaxValues(1,1));
        getOptionDefinitionList().add(new OptionDescriptor<>(new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_SHELL),
                OPTION_DOCKER_COMMAND,
                "Command", "The command that will be executed e.g. to execute bash script inside container", Mode.User)
                .withMinMaxValues(1,1));

        setInitialWidth(400);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellInstance cellInstance, CellExecutionData cellExecutionData) {

            /*
            In the simple case the input and output formats are MIME_TYPE_DATASET_MOLECULE_JSON so we don't need to convert formats.
            In this case there will be just the docker executor step with input named VAR_NAME_INPUT and outout named VAR_NAME_OUTPUT

            If the docker steps needs the input or output to be in different formats (currently only SDF is supported) then we need
            to add steps before and/or after to handle this and if so we need to use temp variables (starting with underscore)
            to pass the data between the steps.

            If the input and output need converting then:
            Step 1 (input convertor): input: VAR_NAME_INPUT, output: _docker_input
            Step 2 (docker): input: _docker_input, output: _docker_output
            Step 3 (output convertor): input: _docker_output, output: VAR_NAME_OUTPUT

            If only input needs converting:
            Step 1 (input convertor): input: VAR_NAME_INPUT, output: _docker_input
            Step 2 (docker): input: _docker_input, output: VAR_NAME_OUTPUT

            If only output needs converting then:
            Step 1 (docker): input: VAR_NAME_INPUT, output: _docker_output
            Step 2 (output convertor): input: _docker_output, output: VAR_NAME_OUTPUT

            If no conversion is needed then:
            Step 2 (output convertor): input: VAR_NAME_INPUT, output: VAR_NAME_OUTPUT

             */

            Long cellId = cellInstance.getId();
            Map<String,Object> options = collectAllOptions(cellInstance);
            String inputType = (String)options.get(OPTION_MEDIA_TYPE_INPUT);
            String outputType = (String)options.get(OPTION_MEDIA_TYPE_OUTPUT);
            List<StepDefinition> steps = new ArrayList<>();

            String inputVarName;
            if (!MIME_TYPE_DATASET_MOLECULE_JSON.equals(inputType)) {
                inputVarName = "_docker_input";
                steps.add(new StepDefinition(StepDefinitionConstants.DatasetServiceExecutor.CLASSNAME)
                        .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, createVariableKey(cellInstance, VAR_NAME_INPUT))
                        .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, inputVarName)
                        .withOption("header.Content-Encoding", "gzip")
                        .withOption("header.Accept-Encoding", "gzip")
                        .withOption("header.Content-Type", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                        .withOption("header.Accept", CommonMimeTypes.MIME_TYPE_MDL_SDF)
                        // TODO - allow user to define which impl to use
                        // TODO - avoid hardcoding the URL - look it up from the service descriptors?
                        .withOption(StepDefinitionConstants.OPTION_SERVICE_ENDPOINT, "http://chemservices:8080/chem-services-cdk-basic/rest/v1/converters/convert_to_sdf"));
            } else {
                inputVarName = VAR_NAME_INPUT;
            }
            String outputVarName;
            StepDefinition outputConvertStep = null;
            if (!MIME_TYPE_DATASET_MOLECULE_JSON.equals(outputType)) {
                if (MIME_TYPE_MDL_SDF.equals(outputType)) {
                    outputVarName = "_docker_output";
                    outputConvertStep = new StepDefinition(StepDefinitionConstants.SdfUpload.CLASSNAME)
                            .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, new VariableKey(cellId, outputVarName))
                            .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, VAR_NAME_OUTPUT);
                } else {
                    throw new IllegalArgumentException("Converting to media type " + outputType + " not supported");
                }
            } else {
                outputVarName = VAR_NAME_OUTPUT;
            }

            // this is the docker executor step
            steps.add(new StepDefinition(StepDefinitionConstants.DockerProcessDataset.CLASSNAME)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new VariableKey(cellId, inputVarName))
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, outputVarName)
                    .withOptions(options));

            if (outputConvertStep != null) {
                steps.add(outputConvertStep);
            }

            LOG.info("Docker cell using variables " + inputVarName + " and " + outputVarName);

            return buildJobDefinition(cellExecutionData, cellInstance, steps.toArray(new StepDefinition[steps.size()]));
        }
    }

}
