package portal.webapp.notebook;


import java.io.Serializable;

public interface VariableChangeListener extends Serializable {

    void onValueChanged(Variable source, Object oldValue);
    void onVariableRemoved(Variable source);
}
