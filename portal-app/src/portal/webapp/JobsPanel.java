package portal.webapp;

import com.im.lac.job.jobdef.JobStatus;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
public class JobsPanel extends Panel {

    @Inject
    private JobsSession jobsSession;
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
                String iconClass = "icon checkmark green";
                if (JobStatus.Status.PENDING.equals(jobStatus.getStatus())) {
                    iconClass = "waiting";
                } else if (JobStatus.Status.RUNNING.equals(jobStatus.getStatus())) {
                    iconClass = "waiting";
                } else if (JobStatus.Status.CANCELLED.equals(jobStatus.getStatus())) {
                    iconClass = "icon remove red";
                } else if (JobStatus.Status.ERROR.equals(jobStatus.getStatus())) {
                    iconClass = "icon remove red";
                }
                WebMarkupContainer status = new WebMarkupContainer("status");
                status.add(new AttributeModifier("class", iconClass));
                listItem.add(status);
            }
        };
        add(jobListView);
    }

    private void refreshJobStatusList() {
        List<JobStatus> jobStatusList = jobsSession.listJobStatuses();
        jobListView.setList(jobStatusList);
    }

    public void refreshJobs() {
        jobListView.setList(jobsSession.listJobStatuses());
        getRequestCycle().find(AjaxRequestTarget.class).add(this);
    }
}
