package portal.webapp;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;
import portal.integration.IntegrationClient;
import portal.integration.PropertyDefinition;

import javax.inject.Inject;
import java.util.Collection;

public class PropertyDefinitionProvider extends TextChoiceProvider<PropertyDefinition> {

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

    }

    @Override
    public Collection<PropertyDefinition> toChoices(Collection<String> collection) {
        return null;
    }
}
