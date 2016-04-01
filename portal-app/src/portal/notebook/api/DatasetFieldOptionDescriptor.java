package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.SimpleTypeDescriptor;
import org.squonk.options.TypeDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class DatasetFieldOptionDescriptor extends OptionDescriptor<String> {

    public DatasetFieldOptionDescriptor(
            @JsonProperty("typeDescriptor") TypeDescriptor<String> typeDescriptor,
            @JsonProperty("key") String key,
            @JsonProperty("label") String label,
            @JsonProperty("description") String description,
            @JsonProperty("values") String[] values,
            @JsonProperty("defaultValue") String defaultValue,
            @JsonProperty("visible") boolean visible,
            @JsonProperty("editable") boolean editable,
            @JsonProperty("minValues") Integer minValues,
            @JsonProperty("maxValues") Integer maxValues,
            @JsonProperty("properties") Map<String, Object> properties) {
        super(typeDescriptor, key, label, description,  values, defaultValue, visible, editable, minValues, maxValues, properties);
    }


    public DatasetFieldOptionDescriptor(String key, String label, String description, String defaultValue) {
        super(new SimpleTypeDescriptor(String.class), key, label, description,  new String[0], defaultValue, true, true, 1, 1);
    }

}
