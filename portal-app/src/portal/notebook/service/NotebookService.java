package portal.notebook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.types.MoleculeObject;
import org.squonk.client.JobStatusClient;
import portal.notebook.api.*;
import toolkit.services.PU;
import toolkit.services.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

@ApplicationScoped
@Transactional
@Path("notebook")
public class NotebookService {

    private static final Logger LOGGER = Logger.getLogger(NotebookService.class.getName());
    @Inject
    @PU(puName = NotebookConstants.PU_NAME)
    private EntityManager entityManager;
    @Inject
    private JobStatusClient jobStatusClient;

    public List<NotebookInfo> listNotebookInfo(String userId) {
        List<NotebookInfo> list = new ArrayList<>();
        TypedQuery<Notebook> query = entityManager.createQuery("select o from Notebook o where o.owner = :owner or o.shared = :shared order by o.name", Notebook.class);
        query.setParameter("owner", userId);
        query.setParameter("shared", true);
        for (Notebook notebookHeader : query.getResultList()) {
            NotebookInfo notebookInfo = new NotebookInfo();
            notebookInfo.setId(notebookHeader.getId());
            notebookInfo.setName(notebookHeader.getName());
            notebookInfo.setDescription(notebookHeader.getDescription());
            notebookInfo.setOwner(notebookHeader.getOwner());
            notebookInfo.setShared(notebookHeader.getShared());
            list.add(notebookInfo);
        }
        return list;
    }

    public NotebookInfo retrieveNotebookInfo(Long id) {
        Notebook notebookHeader = entityManager.find(Notebook.class, id);
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setId(notebookHeader.getId());
        notebookInfo.setName(notebookHeader.getName());
        notebookInfo.setDescription(notebookHeader.getDescription());
        notebookInfo.setOwner(notebookHeader.getOwner());
        notebookInfo.setShared(notebookHeader.getShared());
        return notebookInfo;
    }

    public NotebookInstance findNotebookInstance(Long id) {
        try {
            Notebook notebook = entityManager.find(Notebook.class, id);
            return NotebookInstance.fromBytes(notebook.getData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long createNotebook(EditNotebookData editNotebookData) {
        try {
            Notebook notebook = new Notebook();
            notebook.setName(editNotebookData.getName());
            notebook.setDescription(editNotebookData.getDescription());
            notebook.setOwner(editNotebookData.getOwner());
            notebook.setShared(editNotebookData.getShared());
            notebook.setData(new NotebookInstance().toBytes());
            entityManager.persist(notebook);
            return notebook.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateNotebook(EditNotebookData editNotebookData) {
        Notebook notebook = entityManager.find(Notebook.class, editNotebookData.getId());
        notebook.setName(editNotebookData.getName());
        notebook.setDescription(editNotebookData.getDescription());
        notebook.setOwner(editNotebookData.getOwner());
        notebook.setShared(editNotebookData.getShared());
    }

    public void removeNotebook(Long id) {
        Notebook notebook = entityManager.find(Notebook.class, id);
        TypedQuery<NotebookHistory> historyQuery = entityManager.createQuery("select o from NotebookHistory o where o.notebook = :notebook", NotebookHistory.class);
        historyQuery.setParameter("notebook", notebook);
        for (NotebookHistory notebookHistory : historyQuery.getResultList()) {
            entityManager.remove(notebookHistory);
        }
        entityManager.remove(notebook);
    }

    public Long updateNotebookContents(UpdateNotebookContentsData updateNotebookContentsData) {
        try {                  
            Notebook notebook = entityManager.find(Notebook.class, updateNotebookContentsData.getId());
            doStoreNotebookContents(updateNotebookContentsData.getNotebookInstance(), notebook);
            return notebook.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private void doStoreNotebookContents(NotebookInstance notebookInstance, Notebook notebook) throws Exception {
        NotebookInstance currentNotebookInstance = NotebookInstance.fromBytes(notebook.getData());
        currentNotebookInstance.applyChangesFrom(notebookInstance);
        currentNotebookInstance.resetDirty();
        notebook.setData(currentNotebookInstance.toBytes());
        NotebookHistory notebookHistory = new NotebookHistory();
        notebookHistory.setNotebook(notebook);
        notebookHistory.setData(notebook.getData());
        notebookHistory.setRevisionDate(new Date());
        notebookHistory.setRevisionTime(new Date());
        entityManager.persist(notebookHistory);
        LOGGER.log(Level.INFO, notebookInstance.toJsonString());
    }

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


    public void storeNotebookContents(Long notebookId, NotebookInstance notebookInstance) {
        Notebook notebook = entityManager.find(Notebook.class, notebookId);
        try {
            doStoreNotebookContents(notebookInstance, notebook);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public File resolveContentsFile(Long notebookId, VariableInstance variable) throws Exception {

        File root = new File(System.getProperty("user.home"), "notebook-files");
        if (!root.exists() && !root.mkdirs()) {
            throw new Exception("Could not create " + root.getAbsolutePath());
        }

        File folder = new File(root, "nbk-" + notebookId);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new Exception("Could not create " + folder.getAbsolutePath());
        }

        switch (variable.getVariableDefinition().getVariableType()) {
            case FILE:
                return new File(folder, variable.getCellId() + "-" + variable.getVariableDefinition().getName());
            case STREAM: // fallthrough intended
            case DATASET:
                String fileName = URLEncoder.encode(variable.getCellId() + "_" + variable.getVariableDefinition().getName(), "US-ASCII");
                return new File(folder, fileName);
            default:
                LOGGER.warning("Invalid variable type for file storage: " + variable.getVariableDefinition().getVariableType());
                return null;
        }
    }

    public void storeStreamingContents(Long notebookId, VariableInstance variable, InputStream inputStream) {
        LOGGER.info("storeStreamingContents for " + notebookId + "/" + variable.getVariableDefinition().getName() + "/" + variable.getVariableDefinition().getVariableType());
        try {
            File file = resolveContentsFile(notebookId, variable);
            LOGGER.info("File is " + file);
            OutputStream outputStream = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[4096];
                int r = inputStream.read(buffer);
                while (r > -1) {
                    outputStream.write(buffer, 0, r);
                    r = inputStream.read(buffer);
                }
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void outputStreamingContents(Long notebookId, VariableInstance variable, OutputStream outputStream) {
        try {
            File file = resolveContentsFile(notebookId, variable);
            if (file.exists()) {
                InputStream inputStream = new FileInputStream(file);
                try {
                    byte[] buffer = new byte[4096];
                    int r = inputStream.read(buffer);
                    while (r > -1) {
                        outputStream.write(buffer, 0, r);
                        r = inputStream.read(buffer);
                    }
                    outputStream.flush();
                } finally {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    public Execution executeCell(Long notebookId, Long cellId) {
        try {
            Notebook notebook = entityManager.find(Notebook.class, notebookId);
            Execution execution = findExecution(notebookId, cellId);
            if (execution != null && execution.getJobActive()) {
                throw new RuntimeException("Already running");
            }
            NotebookInstance notebookInstance = NotebookInstance.fromBytes(notebook.getData());
            CellInstance cellInstance = notebookInstance.findCellById(cellId);
            CellDefinition cellDefinition = cellInstance.getCellDefinition();
            CellExecutionData cellExecutionData = new CellExecutionData();
            cellExecutionData.setCellId(cellId);
            cellExecutionData.setNotebookId(notebookId);
            cellExecutionData.setNotebookInstance(notebookInstance);
            JobStatus jobStatus = cellDefinition.getCellExecutor().execute(cellExecutionData);
            if (execution == null) {
                execution = new Execution();
                execution.setNotebook(notebook);
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

    @Path("retrieveNotebookInstance")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public NotebookInstance retrieveNotebookInstance(@QueryParam("notebookId") Long notebookId) {
        NotebookInstance notebookInstance = findNotebookInstance(notebookId);
        return notebookInstance;
    }

    @Path("retrieveCellInstance")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CellInstance retrieveCellInstance(@QueryParam("notebookId") Long notebookId, @QueryParam("cellName") String cellName) {
        NotebookInstance notebookInstance = findNotebookInstance(notebookId);
        return notebookInstance.findCellByName(cellName);
    }


    @Path("readTextValue")
    @GET
    public String readTextValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookInstance notebookInstance = findNotebookInstance(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        return variable.getValue() == null ? null : variable.getValue().toString();
    }


    @Path("readObjectValue")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object readObjectValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookInstance notebookInstance = findNotebookInstance(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        return variable.getValue() == null ? null : variable.getValue();
    }

    @Path("readStreamValue")
    @GET
    public StreamingOutput readStreamValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookInstance notebookInstance = findNotebookInstance(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                outputStreamingContents(notebookId, variable, outputStream);
            }
        };

    }

    @Path("writeTextValue")
    @POST
    public void writeTextValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, @QueryParam("value") String value) {
        NotebookInstance notebookInstance = findNotebookInstance(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        variable.setValue(value);
        storeNotebookContents(notebookId, notebookInstance);
    }

    @Path("writeIntegerValue")
    @POST
    public void writeIntegerValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, @QueryParam("value") Integer value) {
        NotebookInstance notebookInstance = findNotebookInstance(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        variable.setValue(value);
        storeNotebookContents(notebookId, notebookInstance);
    }

    @Path("writeObjectValue")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void writeObjectValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, Object value) {
        NotebookInstance notebookInstance = findNotebookInstance(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        variable.setValue(value);
        storeNotebookContents(notebookId, notebookInstance);
    }

    @Path("writeStreamContents")
    @POST
    public void writeStreamContents(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, InputStream inputStream) {
        NotebookInstance notebookInstance = findNotebookInstance(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        storeStreamingContents(notebookId, variable, inputStream);
    }

    @Path("readFileValueAsMolecules")
    @GET
    public StreamingOutput readFileValueAsMolecules(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                NotebookInstance notebookInstance = findNotebookInstance(notebookId);
                VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
                List<MoleculeObject> list = retrieveFileContentAsMolecules(variable.getValue().toString());
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(outputStream, list);
                outputStream.flush();
            }
        };

    }

    public void commitFileForVariable(Long notebookId, VariableInstance variable) {
        try {
            File file = resolveContentsFile(notebookId, variable);
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            if (!tempFile.exists()) {
                throw new RuntimeException("Temproary file not found: " + tempFile.getAbsolutePath());
            }
            if (file.exists() && !file.delete() ) {
                throw new RuntimeException("Could not remove file " + file.getAbsolutePath());
            }
            if (!tempFile.renameTo(file)) {
                throw new RuntimeException("Could not rename to " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void storeTemporaryFileForVariable(Long notebookId, VariableInstance variable, InputStream inputStream) {
        try {
            File file = resolveContentsFile(notebookId, variable);
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            OutputStream outputStream = new FileOutputStream(tempFile);
            try {
                byte[] buffer = new byte[4096];
                int r = inputStream.read(buffer);
                while (r > -1) {
                    outputStream.write(buffer, 0, r);
                    r = inputStream.read(buffer);
                }
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
