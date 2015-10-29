package portal.notebook;


import java.io.Serializable;

public interface VariableChangeListener extends Serializable {

    void onValueChanged(VariableModel source, Object oldValue);
    void onVariableRemoved(VariableModel source);
}
