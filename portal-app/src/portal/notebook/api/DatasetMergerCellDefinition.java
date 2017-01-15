package portal.notebook.api;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.StepDefinitionConstants.DatasetMerger;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.jobdef.JobDefinition;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.util.CommonMimeTypes;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetMergerCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "DatasetMerger";
    private final static long serialVersionUID = 1l;

    public DatasetMergerCellDefinition() {
        super(CELL_NAME, "Merge datasets into one", "icons/merge.png", new String[]{"merge", "dataset"});
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VariableType.DATASET_ANY));
        getOptionDefinitionList().add(new DatasetsFieldOptionDescriptor(DatasetMerger.OPTION_MERGE_FIELD_NAME, "Merge field name", "Name of value field which identifies equivalent entries"));
        getOptionDefinitionList().add(new OptionDescriptor<>(Boolean.class, DatasetMerger.OPTION_KEEP_FIRST, "When duplicate keep first",
                "When duplicate field name use the existing value rather than the new one", Mode.User)
        .withDefaultValue(true));
        for (int i = 0; i < 5; i++) {
            getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT + (i + 1), VariableType.DATASET_ANY));
        }
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

            // TODO - this should be able to handle BasicObjects too
            IODescriptor[] outputs = new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")};
            List<IODescriptor> inputList = new ArrayList<>();

            StepDefinition step = new StepDefinition(StepDefinitionConstants.DatasetMerger.CLASSNAME)
                    .withOutputs(outputs)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, "output")
                    .withOptions(collectAllOptions(cell));

            for (int i = 1; i <= 5; i++) {
                VariableKey key = createVariableKey(cell, StepDefinitionConstants.VARIABLE_INPUT_DATASET + i);
                if (key != null) {
                    step.withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET + i, key);
                    inputList.add(IODescriptors.createMoleculeObjectDataset("input" + i));
                } else {
                    break;
                }
            }
            IODescriptor[] inputs = inputList.toArray(new IODescriptor[inputList.size()]);
            step.withInputs(inputs);

            return buildJobDefinition(cellExecutionData, cell, inputs, outputs, step);
        }
    }

}
