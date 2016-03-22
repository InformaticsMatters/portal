package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.CsvUpload;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.FileTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

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
        super(CELL_NAME, "CSV upload", "default_icon.png", new String[]{"file", "upload", "csv", "tab"});
        VariableDefinition variableDefinition = new VariableDefinition(VAR_NAME_FILECONTENT, VAR_DISPLAYNAME_FILECONTENT, VariableType.FILE);
        getVariableDefinitionList().add(variableDefinition);
        variableDefinition  = new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET);
        getVariableDefinitionList().add(variableDefinition);
        getOptionDefinitionList().add(new OptionDescriptor<>(new FileTypeDescriptor<File>(new String[] {"csv", "tab", "txt"}),
                OPT_FILE_UPLOAD, "CSV/TAB File", "Upload comma or tab separated text file"));
        getOptionDefinitionList().add(new OptionDescriptor<>(String.class, OPT_FILE_TYPE, "File type", "Type of CSV or TAB file")
                .withValues(new String[]{"TDF", "EXCEL", "MYSQL", "RFC4180", "DEFAULT"})
                .withDefaultValue("DEFAULT"));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, OPT_FIRST_LINE_IS_HEADER, "First line is header",
                "First line contains field names"));

    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {
            CellInstance cell = cellExecutionData.getNotebookInstance().findCellInstanceById(cellExecutionData.getCellId());
            VariableKey key = new VariableKey(cell.getName(), VAR_NAME_FILECONTENT); // we are the producer

            StepDefinition step1 = new StepDefinition(CsvUpload.CLASSNAME)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, key) // maps the input to our own file contents
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cell, step1);
        }
    }

}
