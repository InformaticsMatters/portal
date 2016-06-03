package portal.notebook.api;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.FileTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.SimpleTypeDescriptor;

import java.util.Date;

public class TestCellDefinition extends CellDefinition {
    private final static long serialVersionUID = 1l;

    public TestCellDefinition() {
        super("test", "Test", "icons/file_upload_basic.png", new String[]{"test"});
        OptionDescriptor<String> optionDescriptor = new OptionDescriptor<>(new SimpleTypeDescriptor<>(String.class), "invisible", "Invisible", "Invisible option", null, "default", false, false, null, null);
        getOptionDefinitionList().add(optionDescriptor);
        optionDescriptor = new OptionDescriptor<>(new SimpleTypeDescriptor<>(String.class), "readonly", "Read-only", "Read-only option", null, "default", true, false, null, null);
        getOptionDefinitionList().add(optionDescriptor);
        optionDescriptor = new OptionDescriptor<>(new SimpleTypeDescriptor<>(String.class), "editableWithDefault", "Editable w/default", "Editable with default", null, "default", true, true, null, null);
        getOptionDefinitionList().add(optionDescriptor);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends CellExecutor {
        @Override
        public JobStatus execute(CellInstance cell, CellExecutionData data) throws Exception {
            for (OptionInstance optionInstance : cell.getOptionInstanceMap().values()) {
                System.out.println(optionInstance.getOptionDescriptor().getkey() + ": " + optionInstance.getValue());
            }
            return JobStatus.create(null /* JobDefinition */, "username", new Date(), null).withStatus(JobStatus.Status.COMPLETED, 0, 0, null);
        }
    }

}
