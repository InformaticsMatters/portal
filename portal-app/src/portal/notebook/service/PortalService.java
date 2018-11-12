package portal.notebook.service;

import org.squonk.jobdef.JobStatus;
import org.squonk.client.JobStatusClient;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellExecutionData;
import portal.notebook.api.CellInstance;
import portal.notebook.api.NotebookInstance;
import toolkit.services.PU;
import toolkit.services.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
@Transactional
@Path("portal")
public class PortalService {

    private static final Logger LOGGER = Logger.getLogger(PortalService.class.getName());
    @Inject
    @PU(puName = PortalConstants.PU_NAME)
    private EntityManager entityManager;
    @Inject
    private JobStatusClient jobStatusClient;

    @Path("listActiveExecution")
    @GET
    public List<Execution> listActiveExecution() {
        TypedQuery<Execution> query = entityManager.createQuery("select o from Execution o where o.jobActive = :jobActive", Execution.class);
        query.setParameter("jobActive", Boolean.TRUE);
        return query.getResultList();
    }

    @Path("updateExecutionStatus")
    @POST
    public void updateExecutionStatus(@QueryParam("id") Long id) {
        try {
            Execution execution = entityManager.find(Execution.class, id);
            JobStatus jobStatus = jobStatusClient.get(execution.getJobId());
            JobStatus.Status status = jobStatus.getStatus();
            execution.setJobActive(isActiveStatus(status));
            if (status.equals(JobStatus.Status.COMPLETED)) {
                execution.setJobSuccessful(Boolean.TRUE);
            } else if (status.equals(JobStatus.Status.CANCELLED)) {
                execution.setJobSuccessful(Boolean.FALSE);
            } else if (status.equals(JobStatus.Status.ERROR)) {
                execution.setJobSuccessful(Boolean.FALSE);
            }
            execution.setJobStatus(jobStatus.getStatus());
            execution.setLastEventMessage(jobStatus.getEvents().isEmpty() ? null : jobStatus.getEvents().get(0).toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Boolean isActiveStatus(JobStatus.Status status) {
        if (status.equals(JobStatus.Status.CANCELLED)) {
            return Boolean.FALSE;
        } else if (status.equals(JobStatus.Status.COMPLETED)) {
            return Boolean.FALSE;
        } else if (status.equals(JobStatus.Status.ERROR)) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public Execution executeCell(NotebookInstance notebookInstance, Long notebookId, Long editableId, Long cellId) {
        try {
            TypedQuery<Execution> query = entityManager.createQuery("select o from Execution o where o.notebookId = :notebookId", Execution.class);
            query.setParameter("notebookId", notebookId);
            Execution execution = findExecution(notebookId, cellId);
            if (execution != null && execution.getJobActive()) {
                throw new RuntimeException("Already running");
            }
            CellInstance cellInstance = notebookInstance.findCellInstanceById(cellId);
            CellDefinition cellDefinition = cellInstance.getCellDefinition();
            CellExecutionData cellExecutionData = new CellExecutionData(notebookId, editableId, cellId);
            JobStatus jobStatus = cellDefinition.getCellExecutor().execute(cellInstance, cellExecutionData);
            if (execution == null) {
                execution = new Execution();
                execution.setNotebookId(notebookId);
                execution.setCellId(cellId);
                entityManager.persist(execution);
            }
            execution.setJobId(jobStatus.getJobId());
            execution.setJobStatus(jobStatus.getStatus());
            execution.setJobActive(jobStatus.getStatus() != JobStatus.Status.COMPLETED && jobStatus.getStatus() != JobStatus.Status.ERROR);
            if (jobStatus.getStatus() == JobStatus.Status.COMPLETED) {
                execution.setJobSuccessful(true);
            }
            execution.setLastEventMessage(jobStatus.getEvents().isEmpty() ? null : jobStatus.getEvents().get(0).toString());
            return execution;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Execution findExecution(Long notebookId, Long cellId) {
        TypedQuery<Execution> query = entityManager.createQuery("select o from Execution o where o.notebookId = :notebookId and o.cellId = :cellId", Execution.class);
        query.setParameter("notebookId", notebookId);
        query.setParameter("cellId", cellId);
        return query.getResultList().isEmpty() ? null : query.getResultList().get(0);
    }


}
