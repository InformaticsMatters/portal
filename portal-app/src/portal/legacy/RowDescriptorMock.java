package portal.legacy;

import portal.dataset.IPropertyDescriptor;
import portal.dataset.IRowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RowDescriptorMock implements IRowDescriptor {

    private Long id;
    private String description;
    private Map<Long, PropertyDescriptorMock> propertyDescriptorMap = new HashMap<Long, PropertyDescriptorMock>();
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
        return new ArrayList<IPropertyDescriptor>(propertyDescriptorMap.values());
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

    public void addPropertyDescriptor(PropertyDescriptorMock propertyDescriptor) {
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
