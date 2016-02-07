package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.SdfUpload;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;

/**
 * Created by timbo on 29/01/16.
 */
public class SdfUploadCellDefinition extends CellDefinition {

    public static final String OPT_NAME_FIELD_NAME = SdfUpload.OPTION_NAME_FIELD_NAME;


    public static final String CELL_NAME = "SdfUpload";

    public SdfUploadCellDefinition() {
        setName(CELL_NAME);
        setDescription("SDF upload");
        setExecutable(Boolean.TRUE);
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_FILECONTENT, "File content", VariableType.FILE));
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<String>(
                String.class, OPT_NAME_FIELD_NAME,
                "Name field name", "Name of the field to use for the molecule name (the part before the CTAB block)")
                .withMinValues(0));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            CellInstance cell = cellExecutionData.getNotebookInstance().findCellById(cellExecutionData.getCellId());
            VariableKey key = new VariableKey(cell.getName(), VAR_NAME_FILECONTENT); // we are the producer

            StepDefinition step1 = new StepDefinition(SdfUpload.CLASSNAME)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_FILE_INPUT, key) // maps the input to our own file contents
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cell));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cell, step1);
        }
    }

}
