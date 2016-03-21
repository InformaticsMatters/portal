package portal.notebook.service;

import com.im.lac.job.jobdef.JobStatus;
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
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
@Transactional
@Path("execution")
public class ExecutionService {

    private static final Logger LOGGER = Logger.getLogger(ExecutionService.class.getName());
    @Inject
    @PU(puName = NotebookConstants.PU_NAME)
    private EntityManager entityManager;
    @Inject
    private JobStatusClient jobStatusClient;


    /**

    public List<MoleculeObject> squonkDatasetAsMolecules(Long notebookId, String cellName, String variableName) {
        try {
            NotebookInstance notebookInstance = findNotebookInstance(notebookId);
            VariableInstance variable = notebookInstance.findVariable(cellName, variableName);
            Object metadataString = variable.getValue();
            LOGGER.log(Level.INFO, metadataString == null ?  null : metadataString.toString());
            File file = resolveContentsFile(notebookId, variable);
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(gzipInputStream, new TypeReference<List<MoleculeObject>>() {
                });
            } finally {
                fileInputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public List<MoleculeObject> retrieveFileContentAsMolecules(String fileName) {
        try {
            return parseFile(fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<MoleculeObject> parseFile(String fileName) throws Exception {
        int x = fileName.lastIndexOf(".");
        String ext = fileName.toLowerCase().substring(x + 1);
        if (ext.equals("json")) {
            return parseJson(fileName);
        } else if (ext.equals("tab")) {
            return parseTsv(fileName);
        } else {
            return new ArrayList<>();
        }
    }

    private List<MoleculeObject> parseTsv(String fileName) throws IOException {
        File parent = new File(System.getProperty("user.home"), "notebook-files");
        File file = new File(parent, fileName);
        InputStream inputStream = new FileInputStream(file);
        try {
            List<MoleculeObject> list = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            String[] headers = line.split("\t");
            for (int h = 0; h < headers.length; h++) {
                headers[h] = trim(headers[h]);
            }
            line = bufferedReader.readLine();
            while (line != null) {
                line = line.trim();
                String[] columns = line.split("\t");
                String value = columns[0].trim();
                String smile = value.substring(1, value.length() - 1);
                MoleculeObject object = new MoleculeObject(smile);
                for (int i = 1; i < columns.length; i++) {
                    String name = headers[i];
                    String prop = trim(columns[i]);
                    object.putValue(name, prop);
                }
                list.add(object);
                line = bufferedReader.readLine();
            }
            return list;
        } finally {
            inputStream.close();
        }
    }

    private List<MoleculeObject> parseJson(String fileName) throws Exception {
        File parent = new File(System.getProperty("user.home"), "notebook-files");
        File file = new File(parent, fileName);
        InputStream inputStream = new FileInputStream(file);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream, new TypeReference<List<MoleculeObject>>() {
            });
        } finally {
            inputStream.close();
        }
    }


    private String trim(String v) {
        if (v.length() > 1 && v.charAt(0) == '"' && v.charAt(v.length() - 1) == '"') {
            return v.substring(1, v.length() - 1);
        } else {
            return v;
        }
    }

**/
    public List<Execution> listActiveExecution() {
        TypedQuery<Execution> query = entityManager.createQuery("select o from Execution o where o.jobActive = :jobActive", Execution.class);
        query.setParameter("jobActive", Boolean.TRUE);
        return query.getResultList();
    }

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

    public Execution executeCell(NotebookInstance notebookInstance, Long notebookDescriptorId, Long cellId) {
        try {
            TypedQuery<Execution> query = entityManager.createQuery("select o from Execution o where o.notebookDescriptorId = :notebookDescriptorId", Execution.class);
            query.setParameter("notebookDescriptorId", notebookDescriptorId);
            Execution execution = findExecution(notebookDescriptorId, cellId);
            if (execution != null && execution.getJobActive()) {
                throw new RuntimeException("Already running");
            }
            CellInstance cellInstance = notebookInstance.findCellById(cellId);
            CellDefinition cellDefinition = cellInstance.getCellDefinition();
            CellExecutionData cellExecutionData = new CellExecutionData();
            cellExecutionData.setCellId(cellId);
            cellExecutionData.setNotebookId(notebookDescriptorId);
            cellExecutionData.setNotebookInstance(notebookInstance);
            JobStatus jobStatus = cellDefinition.getCellExecutor().execute(cellExecutionData);
            if (execution == null) {
                execution = new Execution();
                execution.setNotebookDescriptorId(notebookDescriptorId);
                execution.setCellId(cellId);
                entityManager.persist(execution);
            }
            execution.setJobId(jobStatus.getJobId());
            execution.setJobStatus(jobStatus.getStatus());
            execution.setJobActive(Boolean.TRUE);
            return execution;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Execution findExecution(Long notebookId, Long cellId) {
        TypedQuery<Execution> query = entityManager.createQuery("select o from Execution o where o.notebook.id = :notebookId and o.cellId = :cellId", Execution.class);
        query.setParameter("notebookId", notebookId);
        query.setParameter("cellId", cellId);
        return query.getResultList().isEmpty() ? null : query.getResultList().get(0);
    }


}
