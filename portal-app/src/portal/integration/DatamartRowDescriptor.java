package portal.integration;

import portal.service.api.PropertyDescriptor;
import portal.service.api.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DatamartRowDescriptor implements RowDescriptor {

    private Long id;
    private String description;
    private Map<Long, DatamartPropertyDescriptor> propertyDescriptorMap = new HashMap<>();
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
    public List<PropertyDescriptor> listAllPropertyDescriptors() {
        return new ArrayList<>(propertyDescriptorMap.values());
    }

    @Override
    public PropertyDescriptor findPropertyDescriptorById(Long id) {
        return propertyDescriptorMap.get(id);
    }

    @Override
    public PropertyDescriptor getHierarchicalPropertyDescriptor() {
        return propertyDescriptorMap.get(hierarchicalPropertyId);
    }

    @Override
    public PropertyDescriptor getStructurePropertyDescriptor() {
        return propertyDescriptorMap.get(structurePropertyId);
    }

    public void addPropertyDescriptor(DatamartPropertyDescriptor propertyDescriptor) {
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
