package portal.webapp;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;
import portal.integration.IntegrationClient;
import portal.integration.PropertyDefinition;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PropertyDefinitionProvider extends TextChoiceProvider<PropertyDefinition> {

    private List<PropertyDefinition> data;
    @Inject
    private IntegrationClient integrationClient;


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
        data = integrationClient.listPropertyDefinition(s, 20);
        response.addAll(data);
    }

    @Override
    public Collection<PropertyDefinition> toChoices(Collection<String> collection) {
        PropertyDefinition result = null;
        String first = collection.iterator().next();
        for (PropertyDefinition propertyDefinition : data) {
            if (propertyDefinition.getId().equals(first)) {
                result = propertyDefinition;
                break;
            }
        }
        return Arrays.asList(result);
    }
}
