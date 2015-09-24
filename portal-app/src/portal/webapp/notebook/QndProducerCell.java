package portal.webapp.notebook;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QndProducerCell extends AbstractCell {
    private final List<String> outputVariableNameList = new ArrayList<>();


    @Override
    public CellType getCellType() {
        return CellType.QND_PRODUCER;
    }

    @Override
    public List<Variable> getInputVariableList() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getOutputVariableNameList() {
         return outputVariableNameList;

    }
}
