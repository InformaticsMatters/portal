package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.squonk.jobdef.JobStatus;

import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;

@ApplicationScoped
public class CellChangeManager implements Serializable {

    private Listener listener;

    public void notifyDataBindingChanged(CellChangeEvent.DataBinding evt, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onDataBindingChanged(evt, ajaxRequestTarget);
        }
    }

    public void notifyOptionBindingChanged(CellChangeEvent.OptionBinding evt, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onOptionBindingChanged(evt, ajaxRequestTarget);
        }
    }

    public void notifyDataValuesChanged(CellChangeEvent.DataValues evt, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onDataValuesChanged(evt, ajaxRequestTarget);
        }
    }

    public void notifyOptionValuesChanged(CellChangeEvent.OptionValues evt, AjaxRequestTarget ajaxRequestTarget) {
        if (listener != null) {
            listener.onOptionValuesChanged(evt, ajaxRequestTarget);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {

        void onDataBindingChanged(CellChangeEvent.DataBinding evt, AjaxRequestTarget ajaxRequestTarget);

        void onOptionBindingChanged(CellChangeEvent.OptionBinding evt, AjaxRequestTarget ajaxRequestTarget);

        void onDataValuesChanged(CellChangeEvent.DataValues evt, AjaxRequestTarget ajaxRequestTarget);

        void onOptionValuesChanged(CellChangeEvent.OptionValues evt, AjaxRequestTarget ajaxRequestTarget);
    }
}
