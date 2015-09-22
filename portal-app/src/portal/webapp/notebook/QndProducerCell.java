package portal.webapp.notebook;


import java.util.Arrays;
import java.util.List;

public class QndProducerCell extends AbstractCell {
    @Override
    public CellType getCellType() {
        return CellType.QND_PRODUCER;
    }

    @Override
    public List<Variable> getInputVariableList() {
        return null;
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return Arrays.asList("var1", "var2", "var3");
    }
}
