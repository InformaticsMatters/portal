package portal.chemcentral;

import portal.dataset.PropertyDescriptor;
import portal.dataset.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ChemcentralRowDescriptor implements RowDescriptor {

    private Long id;
    private String description;
    private Map<Long, ChemcentralPropertyDescriptor> propertyDescriptorMap = new HashMap<>();
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

    public void addPropertyDescriptor(ChemcentralPropertyDescriptor propertyDescriptor) {
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
