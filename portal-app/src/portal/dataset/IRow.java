package portal.dataset;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public interface IRow extends Serializable {

    Long getId();

    IRowDescriptor getDescriptor();

    Object getProperty(IPropertyDescriptor propertyDescriptor);

    List<IRow> getChildren();

}


