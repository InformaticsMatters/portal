package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;

import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;

@ApplicationScoped
public class ExecutionStatusChangeManager implements Serializable {
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onExecutionstatusChanged(Long cellId, AjaxRequestTarget ajaxRequestTarget);
    }

    public void notifyExecutionStatusChanged(Long cellId, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onExecutionstatusChanged(cellId, ajaxRequestTarget);
        }
    }

}
