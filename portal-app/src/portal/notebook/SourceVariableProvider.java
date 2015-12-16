package portal.notebook;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import java.util.*;

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
        ArrayList<VariableModel> list = new ArrayList<VariableModel>(outputVariableModelMap.values());
        Collections.sort(list, new Comparator<VariableModel>() {
            @Override
            public int compare(VariableModel o1, VariableModel o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        response.addAll(list);
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
