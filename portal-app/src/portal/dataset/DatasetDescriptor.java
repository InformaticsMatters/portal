package portal.dataset;

import com.im.lac.dataset.DataItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class DatasetDescriptor implements IDatasetDescriptor {

    private DataItem dataItem;
    private Map<Long, RowDescriptor> rowDescriptorMap = new HashMap<>();

    public DatasetDescriptor(DataItem dataItem) {
        this.dataItem = dataItem;
    }

    @Override
    public Long getId() {
        return dataItem.getId();
    }

    @Override
    public String getDescription() {
        return dataItem.getName();
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
        return dataItem.getMetadata().getSize();
    }

    public void addRowDescriptor(RowDescriptor rowDescriptor) {
        rowDescriptorMap.put(rowDescriptor.getId(), rowDescriptor);
    }

    public DataItem getDataItem() {
        return dataItem;
    }
}
