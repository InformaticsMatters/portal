package portal.legacy;

import portal.dataset.DatasetDescriptor;
import portal.dataset.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DatasetDescriptorMock implements DatasetDescriptor {

    private Long id;
    private String description;
    private Long datasetMockId;
    private Map<Long, RowDescriptorMock> rowDescriptorMap = new HashMap<>();
    private long rowCount;

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
    public List<RowDescriptor> getAllRowDescriptors() {
        return new ArrayList<>(rowDescriptorMap.values());
    }

    @Override
    public RowDescriptor getRowDescriptorById(Long id) {
        return rowDescriptorMap.get(id);
    }

    @Override
    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public Long getDatasetMockId() {
        return datasetMockId;
    }

    public void setDatasetMockId(Long datasetMockId) {
        this.datasetMockId = datasetMockId;
    }

    public void addRowDescriptor(RowDescriptorMock rowDescriptorMock) {
        rowDescriptorMap.put(rowDescriptorMock.getId(), rowDescriptorMock);
    }

    public void removeRowDescriptor(Long id) {
        rowDescriptorMap.remove(id);
    }

}
