package portal.notebook.service;

import com.im.lac.job.jobdef.JobStatus;
import toolkit.services.AbstractEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"notebook_id", "cellid"})})
@XmlRootElement
public class Execution extends AbstractEntity {
    private Notebook notebook;
    private Long cellId;
    private String jobId;
    private Boolean jobActive;
    private Boolean jobSuccessful;
    private JobStatus.Status jobStatus;


    @ManyToOne
    @JoinColumn(nullable = false)
    public Notebook getNotebook() {
        return notebook;
    }

    public void setNotebook(Notebook notebook) {
        this.notebook = notebook;
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
}
