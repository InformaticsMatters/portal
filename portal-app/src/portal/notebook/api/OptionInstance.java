package portal.notebook.api;

import org.squonk.options.OptionDescriptor;
import org.squonk.options.TypeDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public  class OptionInstance implements Serializable {
    private final static long serialVersionUID = 1l;
    private OptionDescriptor optionDescriptor;
    private Object value;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public OptionDescriptor getOptionDescriptor() {
        return optionDescriptor;
    }

    public void setOptionDescriptor(OptionDescriptor optionDescriptor) {
        this.optionDescriptor = optionDescriptor;
    }
}

