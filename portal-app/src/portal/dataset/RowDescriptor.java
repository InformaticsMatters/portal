package portal.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RowDescriptor implements IRowDescriptor {

    private Long id;
    private String description;
    private Map<Long, PropertyDescriptor> propertyDescriptorMap = new HashMap<>();
    private Long hierarchicalPropertyId;
    private Long structurePropertyId;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<IPropertyDescriptor> listAllPropertyDescriptors() {
        return new ArrayList<>(propertyDescriptorMap.values());
    }

    @Override
    public IPropertyDescriptor findPropertyDescriptorById(Long id) {
        return propertyDescriptorMap.get(id);
    }

    @Override
    public IPropertyDescriptor getHierarchicalPropertyDescriptor() {
        return propertyDescriptorMap.get(hierarchicalPropertyId);
    }

    @Override
    public IPropertyDescriptor getStructurePropertyDescriptor() {
        return propertyDescriptorMap.get(structurePropertyId);
    }

    public void addPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        propertyDescriptorMap.put(propertyDescriptor.getId(), propertyDescriptor);
    }

    public void removePropertyDescriptor(Long id) {
        propertyDescriptorMap.remove(id);
    }

    public Long getHierarchicalPropertyId() {
        return hierarchicalPropertyId;
    }

    public void setHierarchicalPropertyId(Long hierarchicalPropertyId) {
        this.hierarchicalPropertyId = hierarchicalPropertyId;
    }

    public Long getStructurePropertyId() {
        return structurePropertyId;
    }

    public void setStructurePropertyId(Long structurePropertyId) {
        this.structurePropertyId = structurePropertyId;
    }
}
