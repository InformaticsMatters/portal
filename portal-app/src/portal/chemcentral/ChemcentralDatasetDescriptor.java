package portal.chemcentral;

import portal.dataset.IDatasetDescriptor;
import portal.dataset.IRowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class ChemcentralDatasetDescriptor implements IDatasetDescriptor {

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
    public List<IRowDescriptor> getAllRowDescriptors() {
        return new ArrayList<>(rowDescriptorMap.values());
    }

    @Override
    public IRowDescriptor getRowDescriptorById(Long id) {
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
