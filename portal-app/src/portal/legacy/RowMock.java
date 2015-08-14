package portal.legacy;

import portal.dataset.IPropertyDescriptor;
import portal.dataset.IRow;
import portal.dataset.IRowDescriptor;

import java.util.*;

class RowMock implements IRow {

    private Long id;
    private RowDescriptorMock rowDescriptor;
    private Map<PropertyDescriptorMock, Object> properties;
    private List<IRow> children;

    @Override
    public UUID getUuid() {
        return null;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public IRowDescriptor getDescriptor() {
        return rowDescriptor;
    }

    @Override
    public List<IRow> getChildren() {
        return children;
    }

    public void setRowDescriptor(RowDescriptorMock rowDescriptor) {
        this.rowDescriptor = rowDescriptor;
    }

    public void setProperty(PropertyDescriptorMock key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

    public Object getProperty(IPropertyDescriptor propertyDescriptor) {
        Object value = null;
        if (properties != null) {
            value = properties.get(propertyDescriptor);
        }
        return value;
    }

    public RowMock createChild() {
        if (children == null) {
            children = new ArrayList<>();
        }
        RowMock rowMock = new RowMock();
        children.add(rowMock);
        return rowMock;
    }

    public void removeChild(RowMock child) {
        if (children != null) {
            children.remove(child);
        }
    }
}
