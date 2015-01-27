package portal.service.api;

import java.util.List;

public class ListRowFilter {

    private Long datasetDescriptorId;
    private List<Long> rowIdList;

    public Long getDatasetDescriptorId() {
        return datasetDescriptorId;
    }

    public void setDatasetDescriptorId(Long datasetid) {
        this.datasetDescriptorId = datasetid;
    }

    public List<Long> getRowIdList() {
        return rowIdList;
    }

    public void setRowIdList(List<Long> rowIdList) {
        this.rowIdList = rowIdList;
    }
}
