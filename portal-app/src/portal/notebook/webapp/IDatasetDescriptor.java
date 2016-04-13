package portal.notebook.webapp;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public interface IDatasetDescriptor extends Serializable {

    Long getId();

    String getDescription();

    List<IRowDescriptor> getAllRowDescriptors();

    IRowDescriptor getRowDescriptorById(Long id);

    long getRowCount();

}
