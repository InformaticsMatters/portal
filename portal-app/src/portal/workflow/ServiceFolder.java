package portal.workflow;

import com.im.lac.dataset.Metadata;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;

/**
 * @author simetrias
 */
public class ServiceFolder extends ServiceDescriptor {

    public ServiceFolder(String id, String name, String description, String[] tags, String resourceUrl, String[] paths, String owner, String ownerUrl, String[] layers, Class inputClass, Class outputClass, Metadata.Type inputType, Metadata.Type outputType, AccessMode[] accessModes) {
        super(id, name, description, tags, resourceUrl, paths, owner, ownerUrl, layers, inputClass, outputClass, inputType, outputType, accessModes);
    }
}
