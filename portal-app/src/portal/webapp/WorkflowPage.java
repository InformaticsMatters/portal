package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import portal.integration.DatamartSession;
import portal.service.api.DatasetDescriptor;
import portal.service.api.DatasetService;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mariapaz on 4/28/15.
 */
public class WorkflowPage extends WebPage {

    private List<DatasetDescriptor> datasetDescriptorList;
    private ListView<DatasetDescriptor> listView;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService datasetService;
    @Inject
    private DatamartSession datamartSession;

    public WorkflowPage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        datasetDescriptorList = new ArrayList<>();
        addCards();
    }

    private void addCards() {
        listView = new ListView<DatasetDescriptor>("descriptors", new ArrayList<>()) {

            @Override
            protected void populateItem(ListItem<DatasetDescriptor> listItem) {
                DatasetDescriptor datasetDescriptor = listItem.getModelObject();
                listItem.add(new Label("description", datasetDescriptor.getDescription()));
                listItem.add(new Label("lastModified", DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date())));
                listItem.add(new Label("rowCount", datasetDescriptor.getRowCount()));
                listItem.add(new IndicatingAjaxLink("open") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        TreeGridVisualizerPage page = new TreeGridVisualizerPage(datasetDescriptor);
                        setResponsePage(page);
                    }
                });
                listItem.add(new IndicatingAjaxLink("metadata") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {

                    }
                });
            }
        };
        add(listView);
    }

    public void setDatasetDescriptorList(List<DatasetDescriptor> datasetDescriptorList) {
        this.datasetDescriptorList = datasetDescriptorList;
        listView.setList(datasetDescriptorList);
    }

}
