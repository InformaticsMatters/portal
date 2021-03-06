package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public  class OptionInstance implements Serializable {

    private final static long serialVersionUID = 1L;
    private Long cellId;
    private OptionDescriptor optionDescriptor;
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    private Object value;

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

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

