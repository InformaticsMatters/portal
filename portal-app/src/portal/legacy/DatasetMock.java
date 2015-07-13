package portal.legacy;

import portal.dataset.IRow;

import java.util.*;
import java.util.stream.Collectors;

class DatasetMock {

    private Long id;
    private Map<Long, RowMock> rowMap = new HashMap<Long, RowMock>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<IRow> getAllRows() {
        return new ArrayList<>(rowMap.values());
    }

    public void addRow(Long id, RowMock rowMock) {
        rowMap.put(id, rowMock);
    }

    public RowMock getRowById(Long id) {
        return rowMap.get(id);
    }

    public Set<Long> getAllRowIds() {
        return rowMap.keySet();
    }

    public List<IRow> getRowList(List<Long> rowIdList) {
        ArrayList<IRow> result = new ArrayList<>(rowIdList.size());
        result.addAll(rowIdList.stream().map(rowMap::get).collect(Collectors.toList()));
        return result;
    }

    public long getRowCount() {
        return rowMap.size();
    }
}
