package portal.webapp.notebook.persistence;

import toolkit.services.AbstractEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class NotebookHistory extends AbstractEntity {
    private Notebook notebook;
    private Integer revision;
    private Date revisionDate;
    private Date revisionTime;
    private byte[] data;

    @ManyToOne
    @JoinColumn(nullable = false)
    public Notebook getNotebook() {
        return notebook;
    }

    public void setNotebook(Notebook notebook) {
        this.notebook = notebook;
    }

    @Column(nullable = false)
    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    @Temporal(TemporalType.TIME)
    @Column(nullable = false)
    public Date getRevisionTime() {
        return revisionTime;
    }

    public void setRevisionTime(Date revisionTime) {
        this.revisionTime = revisionTime;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
