package portal.service.api;

import java.io.Serializable;
import java.util.List;

public interface DatasetDescriptor extends Serializable {

    Long getId();

    String getDescription();

    List<RowDescriptor> getAllRowDescriptors();

    RowDescriptor getRowDescriptorById(Long id);

    long getRowCount();

}
