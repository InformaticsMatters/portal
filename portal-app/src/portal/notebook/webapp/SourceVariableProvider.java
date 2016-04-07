package portal.notebook.webapp;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import java.util.*;

/**
 * @author simetrias
 */
public class SourceVariableProvider extends TextChoiceProvider<BindingsPanel.VariableInstance> {

    private final Map<String, BindingsPanel.VariableInstance> outputVariableInstanceMap;

    public SourceVariableProvider(Map<String, BindingsPanel.VariableInstance> outputVariableInstanceMap) {
        this.outputVariableInstanceMap = outputVariableInstanceMap;
    }

    @Override
    protected String getDisplayText(BindingsPanel.VariableInstance variableModel) {
        return variableModel.getVariableDefinition().getDisplayName();
    }

    @Override
    protected Object getId(BindingsPanel.VariableInstance variableModel) {
        return variableModel.getVariableDefinition().getName();
    }

    @Override
    public void query(String s, int i, Response<BindingsPanel.VariableInstance> response) {
        ArrayList<BindingsPanel.VariableInstance> list = new ArrayList<>(outputVariableInstanceMap.values());
        Collections.sort(list, new Comparator<BindingsPanel.VariableInstance>() {
            @Override
            public int compare(BindingsPanel.VariableInstance o1, BindingsPanel.VariableInstance o2) {
                return o1.getVariableDefinition().getName().compareTo(o2.getVariableDefinition().getName());
            }
        });
        response.addAll(list);
        response.setHasMore(false);
    }

    @Override
    public Collection<BindingsPanel.VariableInstance> toChoices(Collection<String> collection) {
        String firstChoiceKey = collection.iterator().next();
        BindingsPanel.VariableInstance firstChoice = outputVariableInstanceMap.get(firstChoiceKey);
        ArrayList<BindingsPanel.VariableInstance> result = new ArrayList<>();
        result.add(firstChoice);
        return result;
    }
}
