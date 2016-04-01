package portal.notebook;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;
import org.squonk.notebook.api.VariableInstance;

import java.util.*;

/**
 * @author simetrias
 */
public class SourceVariableProvider extends TextChoiceProvider<VariableInstance> {

    private final Map<String, VariableInstance> outputVariableInstanceMap;

    public SourceVariableProvider(Map<String, VariableInstance> outputVariableInstanceMap) {
        this.outputVariableInstanceMap = outputVariableInstanceMap;
    }

    @Override
    protected String getDisplayText(VariableInstance variableModel) {
        return variableModel.getVariableDefinition().getDisplayName();
    }

    @Override
    protected Object getId(VariableInstance variableModel) {
        return variableModel.getVariableDefinition().getName();
    }

    @Override
    public void query(String s, int i, Response<VariableInstance> response) {
        ArrayList<VariableInstance> list = new ArrayList<>(outputVariableInstanceMap.values());
        Collections.sort(list, new Comparator<VariableInstance>() {
            @Override
            public int compare(VariableInstance o1, VariableInstance o2) {
                return o1.getVariableDefinition().getName().compareTo(o2.getVariableDefinition().getName());
            }
        });
        response.addAll(list);
        response.setHasMore(false);
    }

    @Override
    public Collection<VariableInstance> toChoices(Collection<String> collection) {
        String firstChoiceKey = collection.iterator().next();
        VariableInstance firstChoice = outputVariableInstanceMap.get(firstChoiceKey);
        ArrayList<VariableInstance> result = new ArrayList<>();
        result.add(firstChoice);
        return result;
    }
}
