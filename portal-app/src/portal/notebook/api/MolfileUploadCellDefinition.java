package portal.notebook.api;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.StructureUpload;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.jobdef.JobDefinition;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.FileTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class MolfileUploadCellDefinition extends CellDefinition {
    public static final String OPT_FILE_UPLOAD = StructureUpload.OPTION_FILE_UPLOAD;
    public static final String OPT_FILE_FORMAT = StructureUpload.OPTION_FILE_FORMAT;
    public static final String CELL_NAME = "MolfileUpload";
    private final static long serialVersionUID = 1l;

    public MolfileUploadCellDefinition() {
        super(CELL_NAME, "Molfile upload", "icons/file_upload_molecule.png", new String[]{"file", "upload", "molfile"});
        IODescriptor input = IODescriptors.createMolfile(VAR_NAME_FILECONTENT);
        getVariableDefinitionList().add(input);
        IODescriptor output = IODescriptors.createMoleculeObjectDataset(VAR_NAME_OUTPUT);
        getVariableDefinitionList().add(output);
        getOptionDefinitionList().add(new OptionDescriptor<>(new FileTypeDescriptor(new String[] {"molfile"}), OPT_FILE_UPLOAD, "Molfile", "Upload molfile", Mode.User));
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

            Map<String,Object> opts = collectAllOptions(cell);
            opts.put(OPT_FILE_FORMAT, "mol");

            StepDefinition step1 = new StepDefinition(StructureUpload.CLASSNAME)
                    .withOutputs(outputs)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, key) // maps the input to our own file contents
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, VAR_NAME_OUTPUT)
                    .withOptions(opts);

            return buildJobDefinition(cellExecutionData, cell, null, outputs, step1);
        }
    }

}
