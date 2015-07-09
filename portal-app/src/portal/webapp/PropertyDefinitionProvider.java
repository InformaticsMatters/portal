package portal.webapp;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;
import portal.chemcentral.ChemcentralClient;
import portal.chemcentral.PropertyDefinition;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PropertyDefinitionProvider extends TextChoiceProvider<PropertyDefinition> {

    private List<PropertyDefinition> data;
    @Inject
    private ChemcentralClient chemcentralClient;


    @Override
    protected String getDisplayText(PropertyDefinition propertyDefinition) {
        return propertyDefinition.getPropertyDescription();
    }

    @Override
    protected Object getId(PropertyDefinition propertyDefinition) {
        return propertyDefinition.getId();
    }

    @Override
    public void query(String s, int i, Response<PropertyDefinition> response) {
        data = chemcentralClient.listPropertyDefinition(s, 20);
        response.addAll(data);
    }

    @Override
    public Collection<PropertyDefinition> toChoices(Collection<String> collection) {
        PropertyDefinition result = null;
        Long first = Long.valueOf(collection.iterator().next());
        for (PropertyDefinition propertyDefinition : data) {
            if (propertyDefinition.getId().equals(first)) {
                result = propertyDefinition;
                break;
            }
        }
        return Arrays.asList(result);
    }
}
