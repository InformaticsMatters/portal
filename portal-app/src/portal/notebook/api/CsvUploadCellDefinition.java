package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.CsvUpload;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.jobdef.JobDefinition;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.FileTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.BasicObject;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class CsvUploadCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "CsvUpload";
    public static final String OPT_FILE_UPLOAD = CsvUpload.OPTION_FILE_UPLOAD;
    public static final String OPT_FILE_TYPE = CsvUpload.OPTION_CSV_FORMAT_TYPE;
    public static final String OPT_FIRST_LINE_IS_HEADER = CsvUpload.OPTION_NAME_FIRST_LINE_IS_HEADER;
    private final static long serialVersionUID = 1l;

    public CsvUploadCellDefinition() {
        super(CELL_NAME, "CSV upload", "icons/file_upload_basic.png", new String[]{"file", "upload", "csv", "tab"});

        getVariableDefinitionList().add(IODescriptors.createCSV(VAR_NAME_INPUT));
        getVariableDefinitionList().add(IODescriptors.createBasicObjectDataset(VAR_NAME_OUTPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(new FileTypeDescriptor(new String[] {"csv", "tab", "txt"}),
                OPT_FILE_UPLOAD, "CSV/TAB File", "Upload comma or tab separated text file", Mode.User));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPT_FILE_TYPE, "File type", "Type of CSV or TAB file", Mode.User)
                .withValues(new String[]{"TDF", "EXCEL", "MYSQL", "RFC4180", "DEFAULT"})
                .withDefaultValue("DEFAULT"));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, OPT_FIRST_LINE_IS_HEADER, "First line is header",
                "First line contains field names", Mode.User));

    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

            VariableKey key = new VariableKey(cell.getId(), VAR_NAME_INPUT); // we are the producer
            IODescriptor[] inputs = IODescriptors.createCSVArray(VAR_NAME_INPUT);
            IODescriptor[] outputs = IODescriptors.createBasicObjectDatasetArray(VAR_NAME_OUTPUT);

            StepDefinition step = new StepDefinition(CsvUpload.CLASSNAME)
                    .withInputs(inputs)
                    .withOutputs(outputs)
                    .withInputVariableMapping(VAR_NAME_INPUT, key) // maps the input to our own file contents
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(cellExecutionData, cell, inputs, outputs, step);
        }
    }

}
