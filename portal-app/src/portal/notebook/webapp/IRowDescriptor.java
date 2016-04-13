package portal.notebook.webapp;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public interface IRowDescriptor extends Serializable {

    Long getId();

    String getDescription();

    List<IPropertyDescriptor> listAllPropertyDescriptors();

    IPropertyDescriptor findPropertyDescriptorById(Long id);

    IPropertyDescriptor getHierarchicalPropertyDescriptor();

    IPropertyDescriptor getStructurePropertyDescriptor();

}
