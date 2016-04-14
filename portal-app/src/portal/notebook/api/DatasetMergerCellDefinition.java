package portal.notebook.api;

import com.im.lac.job.jobdef.JobDefinition;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.DatasetMerger;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetMergerCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "DatasetMerger";
    private final static long serialVersionUID = 1l;

    public DatasetMergerCellDefinition() {
        super(CELL_NAME, "Merge datasets into one", "icons/merge.png", new String[]{"merge", "dataset"});
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new DatasetsFieldOptionDescriptor(DatasetMerger.OPTION_MERGE_FIELD_NAME, "Merge field name", "Name of value field which identifies equivalent entries"));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, DatasetMerger.OPTION_KEEP_FIRST, "When duplicate keep first", "When duplicate field name use the existing value rather than the new one")
        .withDefaultValue(true));
        for (int i = 0; i < 5; i++) {
            getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT + (i + 1), "Input dataset " + (i + 1), VariableType.DATASET));
        }
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            NotebookInstance notebook = cellExecutionData.getNotebookInstance();
            CellInstance cell = notebook.findCellInstanceById(cellExecutionData.getCellId());
            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.DatasetMerger.CLASSNAME)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, VAR_NAME_OUTPUT)
                    .withOptions(collectAllOptions(cell));

            for (int i = 1; i <= 5; i++) {
                VariableKey key = createVariableKey(notebook, cell, "input" + i);
                if (key != null) {
                    step1.withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET + i, key);
                } else {
                    break;
                }
            }
            return buildJobDefinition(cellExecutionData.getNotebookId(), cell, step1);
        }
    }

}
