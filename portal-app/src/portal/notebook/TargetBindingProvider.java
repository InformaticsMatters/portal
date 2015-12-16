package portal.notebook;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import java.util.*;

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
        ArrayList<BindingModel> list = new ArrayList<BindingModel>(bindingModelMap.values());
        Collections.sort(list, new Comparator<BindingModel>() {
            @Override
            public int compare(BindingModel o1, BindingModel o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        response.addAll(list);
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
