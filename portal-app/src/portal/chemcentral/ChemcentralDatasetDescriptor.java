package portal.chemcentral;

import portal.dataset.DatasetDescriptor;
import portal.dataset.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class ChemcentralDatasetDescriptor implements DatasetDescriptor {

    private Hitlist hitlist;
    private Map<Long, ChemcentralRowDescriptor> rowDescriptorMap = new HashMap<>();

    public ChemcentralDatasetDescriptor(Hitlist hitlist) {
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

    public void addRowDescriptor(ChemcentralRowDescriptor chemcentralRowDescriptor) {
        rowDescriptorMap.put(chemcentralRowDescriptor.getId(), chemcentralRowDescriptor);
    }
}
