package portal.notebook;

import portal.dataset.IDatasetDescriptor;
import portal.dataset.IRowDescriptor;
import portal.dataset.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TableDisplayDatasetDescriptor implements IDatasetDescriptor {

    private final Long id;
    private final String name;
    private final int size;
    private Map<Long, RowDescriptor> rowDescriptorMap = new HashMap<>();

    public TableDisplayDatasetDescriptor(Long id, String name, int size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public List<IRowDescriptor> getAllRowDescriptors() {
        return new ArrayList<>(rowDescriptorMap.values());
    }

    @Override
    public IRowDescriptor getRowDescriptorById(Long id) {
        return rowDescriptorMap.get(id);
    }

    @Override
    public long getRowCount() {
        return size;
    }

    public void addRowDescriptor(RowDescriptor rowDescriptor) {
        rowDescriptorMap.put(rowDescriptor.getId(), rowDescriptor);
    }


}
