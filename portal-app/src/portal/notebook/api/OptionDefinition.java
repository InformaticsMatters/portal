package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.options.SimpleTypeDescriptor;
import org.squonk.options.TypeDescriptor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OptionDefinition<T> implements Serializable {
    private final TypeDescriptor<T> typeDescriptor;
    private final String key;
    private final String label;
    private final String description;
    private final T[] values;
    private final T defaultValue;
    private final boolean editable;
    private final boolean visible;
    private final Integer minValues;
    private final Integer maxValues;

    public OptionDefinition(@JsonProperty("typeDescriptor") TypeDescriptor<T> typeDescriptor, @JsonProperty("key") String key, @JsonProperty("label") String label, @JsonProperty("description") String description, @JsonProperty("values") T[] values, @JsonProperty("defaultValue") T defaultValue, @JsonProperty("visible") boolean visible, @JsonProperty("editable") boolean editable, @JsonProperty("minValues") Integer minValues, @JsonProperty("maxValues") Integer maxValues) {
        this.typeDescriptor = typeDescriptor;
        this.key = key;
        this.label = label;
        this.description = description;
        this.values = values;
        this.defaultValue = defaultValue;
        this.visible = visible;
        this.editable = editable;
        this.minValues = minValues;
        this.maxValues = maxValues;
    }

    public OptionDefinition(TypeDescriptor<T> type, String key, String label, String description) {
        this(type, key, label, description, null, null, true, true, Integer.valueOf(1), null);
    }

    public OptionDefinition(Class<T> type, String key, String label, String description) {
        this(new SimpleTypeDescriptor(type), key, label, description, null, null, true, true, Integer.valueOf(1), null);
    }

    public OptionDefinition withDefaultValue(T defaultValue) {
        return new OptionDefinition(this.typeDescriptor, this.key, this.label, this.description, this.values, defaultValue, this.visible, this.editable, this.minValues, this.maxValues);
    }

    public OptionDefinition withValues(T[] values) {
        return new OptionDefinition(this.typeDescriptor, this.key, this.label, this.description, values, this.defaultValue, this.visible, this.editable, this.minValues, this.maxValues);
    }

    public OptionDefinition withAccess(boolean visible, boolean editable) {
        return new OptionDefinition(this.typeDescriptor, this.key, this.label, this.description, this.values, this.defaultValue, visible, editable, this.minValues, this.maxValues);
    }

    public OptionDefinition withMinValues(int minValues) {
        return new OptionDefinition(this.typeDescriptor, this.key, this.label, this.description, this.values, this.defaultValue, this.visible, this.editable, Integer.valueOf(minValues), this.maxValues);
    }

    public OptionDefinition withMaxValues(int maxValues) {
        return new OptionDefinition(this.typeDescriptor, this.key, this.label, this.description, this.values, this.defaultValue, this.visible, this.editable, this.minValues, Integer.valueOf(maxValues));
    }

    public String getkey() {
        return this.key;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    public TypeDescriptor<T> getTypeDescriptor() {
        return this.typeDescriptor;
    }

    public T[] getValues() {
        return this.values;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isEditable() {
        return this.editable;
    }

    @JsonIgnore
    public boolean isRequired() {
        return this.minValues == null || this.minValues.intValue() > 0;
    }

    @JsonIgnore
    public String getName() {
        return this.key;
    }

    @JsonIgnore
    public String getDisplayName() {
        return this.label;
    }

    @JsonIgnore
    public OptionType getOptionType() {
        return this.maxValues != null && this.maxValues.intValue() > 1?OptionType.PICKLIST:OptionType.SIMPLE;
    }

    @JsonIgnore
    public List<T> getPicklistValueList() {
        return this.values == null?null: Arrays.asList(this.getValues());
    }
}
