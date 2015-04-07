package portal.integration;

import portal.service.api.DatasetDescriptor;
import portal.service.api.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class DatamartDatasetDescriptor implements DatasetDescriptor {

    private Hitlist hitlist;
    private Map<Long, DatamartRowDescriptor> rowDescriptorMap = new HashMap<>();

    public DatamartDatasetDescriptor(Hitlist hitlist) {
        this.hitlist = hitlist;
    }

    @Override
    public Long getId() {
        return hitlist.getId();
    }

    @Override
    public String getDescription() {
        return hitlist.getName();
    }

    @Override
    public List<RowDescriptor> getAllRowDescriptors() {
        return new ArrayList<>(rowDescriptorMap.values());
    }

    @Override
    public RowDescriptor getRowDescriptorById(Long id) {
        return rowDescriptorMap.get(id);
    }

    @Override
    public long getRowCount() {
        return hitlist.getSize();
    }

    public void addRowDescriptor(DatamartRowDescriptor datamartRowDescriptor) {
        rowDescriptorMap.put(datamartRowDescriptor.getId(), datamartRowDescriptor);
    }
}
