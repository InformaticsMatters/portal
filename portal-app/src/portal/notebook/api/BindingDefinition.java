package portal.notebook.api;


import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XmlRootElement
public class BindingDefinition implements Serializable {
    private final static long serialVersionUID = 1l;
    private String name;
    private final List<VariableType> acceptedVariableTypeList = new ArrayList<>();

    public BindingDefinition() {
    }

    public BindingDefinition(String name, VariableType... acceptedVariableTypes) {
        this.name = name;
        if (acceptedVariableTypes != null) {
            this.acceptedVariableTypeList.addAll(Arrays.asList(acceptedVariableTypes));
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<VariableType> getAcceptedVariableTypeList() {
        return this.acceptedVariableTypeList;
    }
}
