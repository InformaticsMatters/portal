package portal.notebook;

import com.im.lac.job.jobdef.JobStatus;
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
        void onExecutionStatusChanged(Long cellId, JobStatus.Status jobStatus, AjaxRequestTarget ajaxRequestTarget);
    }

    public void notifyExecutionStatusChanged(Long cellId, JobStatus.Status jobStatus, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onExecutionStatusChanged(cellId, jobStatus, ajaxRequestTarget);
        }
    }

}
