package toolkit.services;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
@EntityListeners(TraceListener.class)
public class AbstractTraceableEntity extends AbstractEntity {

    private String persistUsername;
    private Date persistTimestamp;
    private String updateUsername;
    private Date updateTimestamp;

    @Column(nullable = false)
    public String getPersistUsername() {
        return persistUsername;
    }

    public void setPersistUsername(String persistUsername) {
        this.persistUsername = persistUsername;
    }

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getPersistTimestamp() {
        return persistTimestamp;
    }

    public void setPersistTimestamp(Date persistTimestamp) {
        this.persistTimestamp = persistTimestamp;
    }

    @Column(nullable = false)
    public String getUpdateUsername() {
        return updateUsername;
    }

    public void setUpdateUsername(String updateUsername) {
        this.updateUsername = updateUsername;
    }

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Date updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public void copyFromTraceableEntity(AbstractTraceableEntity abstractTraceableEntity) {
        persistUsername = abstractTraceableEntity.getPersistUsername();
        persistTimestamp = abstractTraceableEntity.getPersistTimestamp();
        updateUsername = abstractTraceableEntity.getUpdateUsername();
        updateTimestamp = abstractTraceableEntity.getUpdateTimestamp();
    }

}
