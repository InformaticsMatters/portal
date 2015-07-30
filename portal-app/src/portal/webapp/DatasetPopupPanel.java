package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import portal.dataset.IDatasetDescriptor;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class DatasetPopupPanel extends Panel {

    private final Callbacks callbacks;
    private IDatasetDescriptor datasetDescriptor;

    public DatasetPopupPanel(String id, IDatasetDescriptor datasetDescriptor, Callbacks callbacks) {
        super(id);
        this.callbacks = callbacks;
        this.datasetDescriptor = datasetDescriptor;
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        add(new Label("description", this.datasetDescriptor.getDescription()));
        add(new Label("rowCount", this.datasetDescriptor.getRowCount()));

        add(new IndicatingAjaxLink("open") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
            }
        });

        add(new IndicatingAjaxLink("delete") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onDelete();
            }
        });
    }

    public interface Callbacks extends Serializable {

        void onDelete();

    }
}
