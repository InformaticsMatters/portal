package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import portal.dataset.IDatasetDescriptor;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class DatasetPopupPanel extends Panel {

    private final Callbacks callbacks;
    private IDatasetDescriptor datasetDescriptor;
    @Inject
    private DatasetsSession datasetsSession;

    public DatasetPopupPanel(String id, Callbacks callbacks) {
        super(id);
        this.callbacks = callbacks;
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        add(new Label("description", datasetDescriptor.getDescription()));
        add(new Label("rowCount", datasetDescriptor.getRowCount()));

        add(new IndicatingAjaxLink("open") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
            }
        });

        add(new IndicatingAjaxLink("delete") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                datasetsSession.deleteDataset(datasetDescriptor);
                callbacks.onDelete();
            }
        });
    }

    public void setDatasetDescriptor(IDatasetDescriptor datasetDescriptor) {
        this.datasetDescriptor = datasetDescriptor;
    }

    public interface Callbacks extends Serializable {

        void onDelete();

    }
}
