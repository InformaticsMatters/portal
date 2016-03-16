package portal.notebook.api;

import org.squonk.options.OptionDescriptor;
import org.squonk.options.SimpleTypeDescriptor;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class RestPicklistOptionDescriptor extends OptionDescriptor<String> {
    private final String queryUri;


    public RestPicklistOptionDescriptor(String key, String label, String description, String queryUri) {
        super(new SimpleTypeDescriptor(String.class), key, label, description, new String[0], null, true, true, 1, 1);
        this.queryUri = queryUri;
    }

    public String getQueryUri() {
        return queryUri;
    }

}