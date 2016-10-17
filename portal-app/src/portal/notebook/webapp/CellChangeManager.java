package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.squonk.jobdef.JobStatus;

import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;

@ApplicationScoped
public class CellChangeManager implements Serializable {

    private Listener listener;

    public void notifyExecutionStatusChanged(Long cellId, JobStatus.Status jobStatus, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onExecutionStatusChanged(cellId, jobStatus, ajaxRequestTarget);
        }
    }

    public void notifyVariableChanged(Long cellId, String variableName, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onVariableChanged(cellId, variableName, ajaxRequestTarget);
        }
    }

    public void notifyBindingChanged(Long cellId, String name, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onBindingChanged(cellId, name, ajaxRequestTarget);
        }
    }

    public void notifyOptionBindingChanged(Long cellId, String name, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onOptionBindingChanged(cellId, name, ajaxRequestTarget);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onExecutionStatusChanged(Long cellId, JobStatus.Status jobStatus, AjaxRequestTarget ajaxRequestTarget);

        void onVariableChanged(Long cellId, String variableName, AjaxRequestTarget ajaxRequestTarget);

        void onBindingChanged(Long cellId, String name, AjaxRequestTarget ajaxRequestTarget);

        void onOptionBindingChanged(Long cellId, String name, AjaxRequestTarget ajaxRequestTarget);
    }
}
