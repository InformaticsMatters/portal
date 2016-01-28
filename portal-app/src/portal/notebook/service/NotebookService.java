package portal.notebook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import org.squonk.notebook.api.VariableType;
import portal.notebook.api.NotebookInstance;
import portal.notebook.api.VariableInstance;
import toolkit.services.PU;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

@ApplicationScoped
public class NotebookService {

    @Inject
    @PU(puName = NotebookConstants.PU_NAME)
    private EntityManager entityManager;

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

    public NotebookInstance retrieveNotebookContents(Long id) {
        try {
            Notebook notebookHeader = entityManager.find(Notebook.class, id);
            entityManager.refresh(notebookHeader);
            return NotebookInstance.fromBytes(notebookHeader.getData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long createNotebook(EditNotebookData editNotebookData) {
        try {
            Notebook notebookHeader = new Notebook();
            notebookHeader.setName(editNotebookData.getName());
            notebookHeader.setDescription(editNotebookData.getDescription());
            notebookHeader.setOwner(editNotebookData.getOwner());
            notebookHeader.setShared(editNotebookData.getShared());
            notebookHeader.setData(new NotebookInstance().toBytes());
            entityManager.persist(notebookHeader);
            return notebookHeader.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateNotebook(EditNotebookData editNotebookData) {
        Notebook notebookHeader = entityManager.find(Notebook.class, editNotebookData.getId());
        notebookHeader.setName(editNotebookData.getName());
        notebookHeader.setDescription(editNotebookData.getDescription());
        notebookHeader.setOwner(editNotebookData.getOwner());
        notebookHeader.setShared(editNotebookData.getShared());
    }

    public void removeNotebook(Long id) {
        Notebook notebookHeader = entityManager.find(Notebook.class, id);
        TypedQuery<NotebookHistory> historyQuery = entityManager.createQuery("select o from NotebookHistory o where o.notebook = :notebook", NotebookHistory.class);
        historyQuery.setParameter("notebook", notebookHeader);
        for (NotebookHistory notebookHistory : historyQuery.getResultList()) {
            entityManager.remove(notebookHistory);
        }
        entityManager.remove(notebookHeader);
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
        notebook.setData(notebookInstance.toBytes());
        NotebookHistory notebookHistory = new NotebookHistory();
        notebookHistory.setData(notebook.getData());
        notebookHistory.setRevisionDate(new Date());
        notebookHistory.setRevisionTime(new Date());
        entityManager.persist(notebookHistory);
    }

    public List<MoleculeObject> squonkDatasetAsMolecules(Long notebookId, String cellName, String variableName) {
        try {
            NotebookInstance notebookInstance = retrieveNotebookContents(notebookId);
            VariableInstance variable = notebookInstance.findVariable(cellName, variableName);
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
        File parent = new File(System.getProperty("user.home"), "notebook-files");
        if (!parent.exists() && !parent.mkdirs()) {
            throw new Exception("Couled not create " + parent.getAbsolutePath());
        }
        if (variable.getVariableType().equals(VariableType.FILE)) {
            return new File(parent, variable.getValue().toString());
        }
        if (variable.getVariableType().equals(VariableType.STREAM) || variable.getVariableType().equals(VariableType.DATASET)) {
            String fileName = URLEncoder.encode(variable.getProducerCell().getName() + "_" + variable.getName(), "US-ASCII");
            return new File(parent, fileName);
        } else {
            return null;
        }
    }

    public void storeStreamingContents(Long notebookId, VariableInstance variable, InputStream inputStream) {
        try {
            File file = resolveContentsFile(notebookId, variable);
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
}
