package portal.notebook;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author simetrias
 */
public class TargetBindingProvider extends TextChoiceProvider<BindingModel> {

    private final Map<String, BindingModel> bindingModelMap;

    public TargetBindingProvider(Map<String, BindingModel> bindingModelMap) {
        this.bindingModelMap = bindingModelMap;
    }

    @Override
    protected String getDisplayText(BindingModel bindingModel) {
        return bindingModel.getDisplayName();
    }

    @Override
    protected Object getId(BindingModel bindingModel) {
        return bindingModel.getName();
    }

    @Override
    public void query(String s, int i, Response<BindingModel> response) {
        response.addAll(bindingModelMap.values());
    }

    @Override
    public Collection<BindingModel> toChoices(Collection<String> collection) {
        String firstKey = collection.iterator().next();
        BindingModel firstBindingModel = bindingModelMap.get(firstKey);
        ArrayList<BindingModel> result = new ArrayList<>();
        result.add(firstBindingModel);
        return result;
    }
}
