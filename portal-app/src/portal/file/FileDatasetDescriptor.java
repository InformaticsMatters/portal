package portal.file;

import portal.dataset.IDatasetDescriptor;
import portal.dataset.IRowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FileDatasetDescriptor implements IDatasetDescriptor {

    private Long id;
    private String description;
    private Long fileDatasetId;
    private Map<Long, FileRowDescriptor> rowDescriptorMap = new HashMap<>();
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
    public List<IRowDescriptor> getAllRowDescriptors() {
        return new ArrayList<>(rowDescriptorMap.values());
    }

    @Override
    public IRowDescriptor getRowDescriptorById(Long id) {
        return rowDescriptorMap.get(id);
    }

    @Override
    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public Long getFileDatasetId() {
        return fileDatasetId;
    }

    public void setFileDatasetId(Long fileDatasetId) {
        this.fileDatasetId = fileDatasetId;
    }

    public void addRowDescriptor(FileRowDescriptor fileRowDescriptor) {
        rowDescriptorMap.put(fileRowDescriptor.getId(), fileRowDescriptor);
    }

    public void removeRowDescriptor(Long id) {
        rowDescriptorMap.remove(id);
    }

}
