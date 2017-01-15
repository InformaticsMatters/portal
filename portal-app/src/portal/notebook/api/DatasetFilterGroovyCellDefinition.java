package portal.notebook.api;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetFilterGroovyCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "GroovyDatasetFilter";
    private final static long serialVersionUID = 1l;


    public DatasetFilterGroovyCellDefinition() {
        super(CELL_NAME, "Filter dataset (Groovy)", "icons/program_filter.png", new String[]{"script", "groovy", "filter", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition("input", VariableType.DATASET_ANY));
        getVariableDefinitionList().add(new VariableDefinition("output", VariableType.DATASET_ANY));
        getOptionDefinitionList().add(new OptionDescriptor<>(
                new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "script", "Filter (Groovy expression)", "Filter as groovy expression. e.g. logp < 5 && molweight < 500", Mode.User));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.DatasetFilterGroovy.CLASSNAME);
    }

}
