package portal.notebook.cells;

import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.*;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by timbo on 29/01/16.
 */
@XmlRootElement
public class DatasetFilterGroovyCellDefinition extends CellDefinition {
    public static final String CELL_NAME = "GroovyDatasetFilter";
    private final static long serialVersionUID = 1l;


    public DatasetFilterGroovyCellDefinition() {
        super(CELL_NAME, "Filter dataset (Groovy)", "default_icon.png", new String[]{"script", "groovy", "filter", "dataset"});
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
        getOptionDefinitionList().add(new OptionDescriptor<>(
                new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "script", "Filter (Groovy expression)", "Filter as groovy expression. e.g. logp < 5 && molweight < 500"));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new SimpleJobCellExecutor(StepDefinitionConstants.DatasetFilterGroovy.CLASSNAME);
    }

}
