package portal.webapp;

import portal.dataset.IDatasetDescriptor;
import portal.dataset.IPropertyDescriptor;
import portal.dataset.IRowDescriptor;

import java.io.Serializable;

public class DescriptorNodeData implements Serializable {

    private Object descriptor;

    public DescriptorNodeData(Object descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescription() {
        String result = "defaultValue";

        if (descriptor != null) {
            if (descriptor instanceof IDatasetDescriptor) {
                result = ((IDatasetDescriptor) descriptor).getDescription();
            } else if (descriptor instanceof IRowDescriptor) {
                result = ((IRowDescriptor) descriptor).getDescription();
            } else if (descriptor instanceof IPropertyDescriptor) {
                result = ((IPropertyDescriptor) descriptor).getDescription();
            }
        }
        return result;
    }
}


