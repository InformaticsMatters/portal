package portal.service.api;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public interface RowDescriptor extends Serializable {

    Long getId();

    String getDescription();

    List<PropertyDescriptor> listAllPropertyDescriptors();

    PropertyDescriptor findPropertyDescriptorById(Long id);

    PropertyDescriptor getHierarchicalPropertyDescriptor();

    PropertyDescriptor getStructurePropertyDescriptor();

}
