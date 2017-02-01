package portal.notebook.api;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.SdfUpload;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.jobdef.JobDefinition;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.FileTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class SdfUploadCellDefinition extends CellDefinition {
    public static final String OPT_NAME_FIELD_NAME = SdfUpload.OPTION_NAME_FIELD_NAME;
    public static final String OPT_FILE_UPLOAD = SdfUpload.OPTION_FILE_UPLOAD;
    public static final String CELL_NAME = "SdfUpload";
    private final static long serialVersionUID = 1l;

    public SdfUploadCellDefinition() {
        super(CELL_NAME, "SDF upload", "icons/file_upload_molecule.png", new String[]{"file", "upload", "sdf"});
        IODescriptor input = IODescriptors.createSDF(VAR_NAME_FILECONTENT);
        getVariableDefinitionList().add(input);
        IODescriptor output = IODescriptors.createMoleculeObjectDataset(VAR_NAME_OUTPUT);
        getVariableDefinitionList().add(output);
        getOptionDefinitionList().add(new OptionDescriptor<>(new FileTypeDescriptor(new String[] {"sdf"}), OPT_FILE_UPLOAD, "SD File", "Upload SD file", Mode.User));
        getOptionDefinitionList().add(new OptionDescriptor<>(
                String.class, OPT_NAME_FIELD_NAME,
                "Name field name", "Name of the field to use for the molecule name (the part before the CTAB block)", Mode.User)
                .withMinMaxValues(0,1));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

            VariableKey key = new VariableKey(cellExecutionData.getCellId(), VAR_NAME_FILECONTENT); // we are the producer
            IODescriptor[] outputs = IODescriptors.createBasicObjectDatasetArray(VAR_NAME_OUTPUT);

            StepDefinition step1 = new StepDefinition(SdfUpload.CLASSNAME)
                    .withOutputs(outputs)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, key) // maps the input to our own file contents
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(cellExecutionData, cell, null, outputs, step1);
        }
    }

}
