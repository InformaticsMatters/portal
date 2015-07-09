package portal.chemcentral;

import portal.dataset.Row;
import portal.dataset.RowDescriptor;
import portal.service.api.PropertyDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ChemcentralRow implements Row {

    private Long id;
    private ChemcentralRowDescriptor rowDescriptor;
    private Map<ChemcentralPropertyDescriptor, Object> properties;
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

    public void setDescriptor(ChemcentralRowDescriptor rowDescriptor) {
        this.rowDescriptor = rowDescriptor;
    }

    @Override
    public List<Row> getChildren() {
        return children;
    }

    public void setProperty(ChemcentralPropertyDescriptor key, Object value) {
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

    public ChemcentralRow createChild() {
        if (children == null) {
            children = new ArrayList<>();
        }
        ChemcentralRow rowMock = new ChemcentralRow();
        children.add(rowMock);
        return rowMock;
    }

    public void removeChild(ChemcentralRow child) {
        if (children != null) {
            children.remove(child);
        }
    }
}
