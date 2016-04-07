package portal.notebook.webapp;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import java.util.*;

/**
 * @author simetrias
 */
public class TargetBindingProvider extends TextChoiceProvider<BindingsPanel.BindingInstance> {

    private final Map<String, BindingsPanel.BindingInstance> bindingModelMap;

    public TargetBindingProvider(Map<String, BindingsPanel.BindingInstance> bindingModelMap) {
        this.bindingModelMap = bindingModelMap;
    }

    @Override
    protected String getDisplayText(BindingsPanel.BindingInstance bindingModel) {
        return bindingModel.getDisplayName();
    }

    @Override
    protected Object getId(BindingsPanel.BindingInstance bindingModel) {
        return bindingModel.getName();
    }

    @Override
    public void query(String s, int i, Response<BindingsPanel.BindingInstance> response) {
        ArrayList<BindingsPanel.BindingInstance> list = new ArrayList<BindingsPanel.BindingInstance>(bindingModelMap.values());
        Collections.sort(list, new Comparator<BindingsPanel.BindingInstance>() {
            @Override
            public int compare(BindingsPanel.BindingInstance o1, BindingsPanel.BindingInstance o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        response.addAll(list);
    }

    @Override
    public Collection<BindingsPanel.BindingInstance> toChoices(Collection<String> collection) {
        String firstKey = collection.iterator().next();
        BindingsPanel.BindingInstance firstBindingInstance = bindingModelMap.get(firstKey);
        ArrayList<BindingsPanel.BindingInstance> result = new ArrayList<>();
        result.add(firstBindingInstance);
        return result;
    }
}
