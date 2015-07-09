package portal.file;

import portal.dataset.Row;
import portal.dataset.RowDescriptor;
import portal.service.api.PropertyDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FileRow implements Row {

    private Long id;
    private FileRowDescriptor rowDescriptor;
    private Map<FilePropertyDescriptor, Object> properties;
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

    @Override
    public List<Row> getChildren() {
        return children;
    }

    public void setRowDescriptor(FileRowDescriptor rowDescriptor) {
        this.rowDescriptor = rowDescriptor;
    }

    public void setProperty(FilePropertyDescriptor key, Object value) {
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

    public FileRow createChild() {
        if (children == null) {
            children = new ArrayList<>();
        }
        FileRow fileRow = new FileRow();
        children.add(fileRow);
        return fileRow;
    }

    public void removeChild(FileRow child) {
        if (children != null) {
            children.remove(child);
        }
    }
}
