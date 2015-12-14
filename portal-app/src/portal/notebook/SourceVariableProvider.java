package portal.notebook;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author simetrias
 */
public class SourceVariableProvider extends TextChoiceProvider<VariableModel> {

    private final Map<String, VariableModel> outputVariableModelMap;

    public SourceVariableProvider(Map<String, VariableModel> outputVariableModelMap) {
        this.outputVariableModelMap = outputVariableModelMap;
    }

    @Override
    protected String getDisplayText(VariableModel variableModel) {
        return variableModel.getDisplayName();
    }

    @Override
    protected Object getId(VariableModel variableModel) {
        return variableModel.getName();
    }

    @Override
    public void query(String s, int i, Response<VariableModel> response) {
        response.addAll(outputVariableModelMap.values());
        response.setHasMore(false);
    }

    @Override
    public Collection<VariableModel> toChoices(Collection<String> collection) {
        String firstChoiceKey = collection.iterator().next();
        VariableModel firstChoice = outputVariableModelMap.get(firstChoiceKey);
        ArrayList<VariableModel> result = new ArrayList<>();
        result.add(firstChoice);
        return result;
    }
}
