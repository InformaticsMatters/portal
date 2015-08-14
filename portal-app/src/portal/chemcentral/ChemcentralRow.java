package portal.chemcentral;

import portal.dataset.IPropertyDescriptor;
import portal.dataset.IRow;
import portal.dataset.IRowDescriptor;

import java.util.*;

class ChemcentralRow implements IRow {

    private Long id;
    private ChemcentralRowDescriptor rowDescriptor;
    private Map<ChemcentralPropertyDescriptor, Object> properties;
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

    public void setDescriptor(ChemcentralRowDescriptor rowDescriptor) {
        this.rowDescriptor = rowDescriptor;
    }

    @Override
    public List<IRow> getChildren() {
        return children;
    }

    public void setProperty(ChemcentralPropertyDescriptor key, Object value) {
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
