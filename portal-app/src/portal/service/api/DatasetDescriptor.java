package portal.service.api;

import java.io.Serializable;
import java.util.List;

public interface DatasetDescriptor extends Serializable {

    Long getId();

    String getDescription();

    List<RowDescriptor> listAllRowDescriptors();

    RowDescriptor findRowDescriptorById(Long id);

    default public void doSomeOtherWork() {
        System.out.println("default method");
    }
}
