package portal.notebook.api;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.CellExecutorJobDefinition;
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
public class MolfileUploadCellDefinition extends CellDefinition {

    public static final String CELL_NAME = "MolfileUpload";
    private final static long serialVersionUID = 1l;

    public MolfileUploadCellDefinition() {
        super(CELL_NAME, "Molfile upload", "icons/file_upload_molecule.png", new String[]{"file", "upload", "molfile"});
        getVariableDefinitionList().add(IODescriptors.createMoleculeObjectDataset(VAR_NAME_OUTPUT));
        getVariableDefinitionList().add(IODescriptors.createMolfile(VAR_NAME_INPUT));
        getOptionDefinitionList().add(new OptionDescriptor<>(new FileTypeDescriptor(new String[] {"molfile"}), "input", "Molfile", "Upload molfile", Mode.User));
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    static class Executor extends AbstractJobCellExecutor {

        @Override
        protected CellExecutorJobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData) {

            VariableKey key = new VariableKey(cellExecutionData.getCellId(), VAR_NAME_INPUT); // we are the producer
            IODescriptor[] inputs = new IODescriptor[] {IODescriptors.createMolfile(VAR_NAME_INPUT)};
            IODescriptor[] outputs = IODescriptors.createMoleculeObjectDatasetArray(VAR_NAME_OUTPUT);

            //StepDefinition step1 = new StepDefinition(MolfileUpload.CLASSNAME)
            StepDefinition step = new StepDefinition("org.squonk.execution.steps.impl.MolfileReaderStep")
                    .withInputs(inputs)
                    .withOutputs(outputs)
                    .withInputVariableMapping(VAR_NAME_INPUT, key); // maps the input to our own file contents

            return buildJobDefinition(cellExecutionData, cell, inputs, outputs, step);
        }
    }

}
