package portal.notebook;

import com.squonk.notebook.api.VariableType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BindingModel implements Serializable {
    private String displayName;
    private String name;
    private final List<VariableType> acceptedVariableTypeList = new ArrayList<>();
    private VariableModel sourceVariableModel;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VariableModel getSourceVariableModel() {
        return sourceVariableModel;
    }

    public void setSourceVariableModel(VariableModel sourceVariableModel) {
        this.sourceVariableModel = sourceVariableModel;
    }

    public List<VariableType> getAcceptedVariableTypeList() {
        return acceptedVariableTypeList;
    }
}
