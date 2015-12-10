package portal.notebook;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import java.util.Collection;

/**
 * @author simetrias
 */
public class InputProvider extends TextChoiceProvider<BindingModel> {


    @Override
    protected String getDisplayText(BindingModel bindingModel) {
        return null;
    }

    @Override
    protected Object getId(BindingModel bindingModel) {
        return null;
    }

    @Override
    public void query(String s, int i, Response<BindingModel> response) {

    }

    @Override
    public Collection<BindingModel> toChoices(Collection<String> collection) {
        return null;
    }
}
