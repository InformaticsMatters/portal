package portal.notebook.service;

import org.squonk.jobdef.JobStatus;
import org.squonk.util.IOUtils;
import toolkit.services.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"notebookid", "cellid"})})
@XmlRootElement
public class Execution extends AbstractEntity {
    private Long notebookId;
    private Long cellId;
    private String jobId;
    private Boolean jobActive;
    private Boolean jobSuccessful;
    private JobStatus.Status jobStatus;
    private String lastEventMessage;
    private String additionalInfo;

    @Column(nullable = false)
    public Long getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(Long notebookId) {
        this.notebookId = notebookId;
    }

    @Column(nullable = false)
    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Boolean getJobActive() {
        return jobActive;
    }

    public void setJobActive(Boolean jobActive) {
        this.jobActive = jobActive;
    }

    public Boolean getJobSuccessful() {
        return jobSuccessful;
    }

    public void setJobSuccessful(Boolean jobSuccessful) {
        this.jobSuccessful = jobSuccessful;
    }

    public JobStatus.Status getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus.Status jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getLastEventMessage() {
        return lastEventMessage;
    }

    public void setLastEventMessage(String lastEventMessage) {
        this.lastEventMessage = IOUtils.truncateString(lastEventMessage, 255);
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
