package portal.notebook.api;

import org.squonk.options.OptionDescriptor;
import org.squonk.options.SimpleTypeDescriptor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatasetsFieldOptionDescriptor extends OptionDescriptor<String> {

    public DatasetsFieldOptionDescriptor(String key, String label, String description) {
        super(new SimpleTypeDescriptor(String.class), key, label, description, new String[0], null, true, true, 1, 1);
    }

}
