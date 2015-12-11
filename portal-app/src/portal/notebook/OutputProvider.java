package portal.notebook;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import java.util.Collection;

/**
 * @author simetrias
 */
public class OutputProvider extends TextChoiceProvider<VariableModel> {


    @Override
    protected String getDisplayText(VariableModel variableModel) {
        return null;
    }

    @Override
    protected Object getId(VariableModel variableModel) {
        return null;
    }

    @Override
    public void query(String s, int i, Response<VariableModel> response) {

    }

    @Override
    public Collection<VariableModel> toChoices(Collection<String> collection) {
        return null;
    }
}
