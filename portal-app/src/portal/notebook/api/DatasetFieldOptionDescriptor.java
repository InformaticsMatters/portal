package portal.notebook.api;

import org.squonk.options.OptionDescriptor;
import org.squonk.options.SimpleTypeDescriptor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatasetFieldOptionDescriptor extends OptionDescriptor<String> {

    public DatasetFieldOptionDescriptor(String key, String label, String description, String defaultValue) {
        super(new SimpleTypeDescriptor(String.class), key, label, description,  new String[0], defaultValue, true, true, 1, 1);
    }

}
