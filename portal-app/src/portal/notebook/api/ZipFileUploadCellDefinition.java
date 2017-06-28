package portal.notebook.api;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.ZipFileUpload;
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
public class ZipFileUploadCellDefinition extends CellDefinition {

    public static final String OPT_FILE_UPLOAD = ZipFileUpload.OPTION_FILE_UPLOAD;
    public static final String CELL_NAME = "ZipFileUpload";
    private final static long serialVersionUID = 1l;
    private static final String ZIP_OUTPUT = ZipFileUpload.VARIABLE_ZIP;

    public ZipFileUploadCellDefinition() {
        super(CELL_NAME, "ZIP file upload", "icons/file_upload_basic.png", new String[]{"file", "upload", "zip"});
        IODescriptor input = IODescriptors.createZipFile(VAR_NAME_FILECONTENT);
        getVariableDefinitionList().add(input);
        getOptionDefinitionList().add(new OptionDescriptor<>(new FileTypeDescriptor(new String[] {"zipfile"}), OPT_FILE_UPLOAD, "ZIP file", "Upload ZIP file", Mode.User));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

            VariableKey key = new VariableKey(cellExecutionData.getCellId(), VAR_NAME_FILECONTENT); // we are the producer
            IODescriptor[] outputs = new IODescriptor[] {IODescriptors.createZipFile(ZIP_OUTPUT)};

            Map<String,Object> opts = collectAllOptions(cell);

            StepDefinition step1 = new StepDefinition(ZipFileUpload.CLASSNAME)
                    .withOutputs(outputs)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, key) // maps the input to our own file contents
                    .withOutputVariableMapping(VAR_NAME_OUTPUT, VAR_NAME_OUTPUT)
                    .withOptions(opts);

            return buildJobDefinition(cellExecutionData, cell, null, outputs, step1);
        }
    }

}
