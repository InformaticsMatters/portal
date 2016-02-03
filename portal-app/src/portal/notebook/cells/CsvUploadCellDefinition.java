package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.CsvUpload;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;

/**
 * Created by timbo on 29/01/16.
 */
public class CsvUploadCellDefinition extends CellDefinition {

    public static final String CELL_NAME = "CsvUpload";

    public CsvUploadCellDefinition() {
        setName(CELL_NAME);
        setDescription("CSV upload");
        setExecutable(Boolean.TRUE);
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_FILECONTENT, "File content", VariableType.FILE));
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<String>(String.class, CsvUpload.OPTION_NAME_FILE_TYPE, "File type",
                "Type of CSV or TAB file")
                .withValues(new String[]{"TDF", "EXCEL", "MYSQL", "RFC4180", "DEFAULT"})
                .withDefaultValue("DEFAULT"));
        getOptionDefinitionList().add(new OptionDescriptor<Boolean>(Boolean.class,  CsvUpload.OPTION_NAME_FIRST_LINE_IS_HEADER, "First line is header",
                "First line contains field names"));

    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {
            CellInstance cellInstance = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(CsvUpload.CLASSNAME)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cellInstance));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cellInstance, step1);
        }
    }

}
