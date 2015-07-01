package portal.datamart;

import portal.service.api.PropertyDescriptor;
import portal.service.api.Row;
import portal.service.api.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DatamartRow implements Row {

    private Long id;
    private DatamartRowDescriptor rowDescriptor;
    private Map<DatamartPropertyDescriptor, Object> properties;
    private List<Row> children;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public RowDescriptor getDescriptor() {
        return rowDescriptor;
    }

    public void setDescriptor(DatamartRowDescriptor rowDescriptor) {
        this.rowDescriptor = rowDescriptor;
    }

    @Override
    public List<Row> getChildren() {
        return children;
    }

    public void setProperty(DatamartPropertyDescriptor key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

    public Object getProperty(PropertyDescriptor propertyDescriptor) {
        Object value = null;
        if (properties != null) {
            value = properties.get(propertyDescriptor);
        }
        return value;
    }

    public DatamartRow createChild() {
        if (children == null) {
            children = new ArrayList<>();
        }
        DatamartRow rowMock = new DatamartRow();
        children.add(rowMock);
        return rowMock;
    }

    public void removeChild(DatamartRow child) {
        if (children != null) {
            children.remove(child);
        }
    }
}
