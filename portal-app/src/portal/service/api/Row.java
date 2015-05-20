package portal.service.api;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public interface Row extends Serializable {

    Long getId();

    RowDescriptor getDescriptor();

    Object getProperty(PropertyDescriptor propertyDescriptor);

    List<Row> getChildren();

}


