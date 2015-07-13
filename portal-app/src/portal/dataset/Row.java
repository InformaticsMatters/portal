package portal.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Row implements IRow {

    private Long id;
    private RowDescriptor rowDescriptor;
    private Map<PropertyDescriptor, Object> properties;
    private List<IRow> children;

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

    public void setDescriptor(RowDescriptor rowDescriptor) {
        this.rowDescriptor = rowDescriptor;
    }

    @Override
    public List<IRow> getChildren() {
        return children;
    }

    public void setProperty(PropertyDescriptor key, Object value) {
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

    public Row createChild() {
        if (children == null) {
            children = new ArrayList<>();
        }
        Row rowMock = new Row();
        children.add(rowMock);
        return rowMock;
    }

    public void removeChild(Row child) {
        if (children != null) {
            children.remove(child);
        }
    }
}
