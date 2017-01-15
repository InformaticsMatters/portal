package portal.notebook.webapp;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;
import portal.notebook.api.BindingInstance;

import java.util.*;

/**
 * @author simetrias
 */
public class TargetBindingProvider extends TextChoiceProvider<BindingInstance> {

    private final Map<String, BindingInstance> bindingModelMap;

    public TargetBindingProvider(Map<String, BindingInstance> bindingModelMap) {
        this.bindingModelMap = bindingModelMap;
    }

    @Override
    protected String getDisplayText(BindingInstance bindingModel) {
        return bindingModel.getName();
    }

    @Override
    protected Object getId(BindingInstance bindingModel) {
        return bindingModel.getName();
    }

    @Override
    public void query(String s, int i, Response<BindingInstance> response) {
        ArrayList<BindingInstance> list = new ArrayList<BindingInstance>(bindingModelMap.values());
        Collections.sort(list, new Comparator<BindingInstance>() {
            @Override
            public int compare(BindingInstance o1, BindingInstance o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        response.addAll(list);
    }

    @Override
    public Collection<BindingInstance> toChoices(Collection<String> collection) {
        String firstKey = collection.iterator().next();
        BindingInstance firstBindingInstance = bindingModelMap.get(firstKey);
        ArrayList<BindingInstance> result = new ArrayList<>();
        result.add(firstBindingInstance);
        return result;
    }
}
