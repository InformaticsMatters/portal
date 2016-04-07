package portal.notebook.webapp;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author simetrias
 */
public interface IRow extends Serializable {

    UUID getUuid();

    Long getId();

    IRowDescriptor getDescriptor();

    Object getProperty(IPropertyDescriptor propertyDescriptor);

    List<IRow> getChildren();

}


