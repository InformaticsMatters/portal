package portal.webapp;

import com.im.lac.job.jobdef.AsyncLocalProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.DatasetJobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.types.MoleculeObject;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import portal.dataset.IDatasetDescriptor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
public class JobsPanel extends Panel {

    @Inject
    private JobsSession jobsSession;
    @Inject
    private DatasetsSession datasetsSession;
    private ListView<JobStatus> jobListView;

    public JobsPanel(String id) {
        super(id);
        addJobListView();
        refreshJobStatusList();
    }

    private void addJobListView() {
        jobListView = new ListView<JobStatus>("jobs", new ArrayList<>()) {

            @Override
            protected void populateItem(ListItem<JobStatus> listItem) {
                JobStatus jobStatus = listItem.getModelObject();
                listItem.add(new Label("id", jobStatus.getJobId()));
                listItem.add(new Label("status", jobStatus.getStatus()));
            }
        };
        add(jobListView);
    }

    private void refreshJobStatusList() {
        List<JobStatus> jobStatusList = jobsSession.listJobStatuses();
        jobListView.setList(jobStatusList);
    }

    private void testJobSubmission() {
        List<IDatasetDescriptor> datasetList = datasetsSession.listDatasetDescriptors(new DatasetFilterData());

        AsyncLocalProcessDatasetJobDefinition jobDefinition = new AsyncLocalProcessDatasetJobDefinition(
                datasetList.get(0).getId(),
                "direct:simpleroute",
                DatasetJobDefinition.DatasetMode.CREATE,
                MoleculeObject.class,
                "Gustavo 1");

        jobsSession.submitJob(jobDefinition);
    }


}
