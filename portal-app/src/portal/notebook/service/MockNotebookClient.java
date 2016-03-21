package portal.notebook.service;

import org.squonk.client.NotebookClient;
import org.squonk.notebook.api2.NotebookDescriptor;
import org.squonk.notebook.api2.NotebookEditable;
import org.squonk.notebook.api2.NotebookSavepoint;
import portal.notebook.api.NotebookInstance;
import toolkit.services.PU;
import toolkit.services.Transactional;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Alternative
@RequestScoped
@Transactional
public class MockNotebookClient implements NotebookClient {
    @Inject
    @PU(puName = PortalConstants.PU_NAME)
    private EntityManager entityManager;

    @Override
    public NotebookDescriptor createNotebook(String s, String s1, String s2) throws Exception {
        MockNotebookDescriptor mockNotebookDescriptor = new MockNotebookDescriptor();
        mockNotebookDescriptor.setOwner(s);
        mockNotebookDescriptor.setName(s1);
        mockNotebookDescriptor.setDescription(s2);
        entityManager.persist(mockNotebookDescriptor);
        NotebookDescriptor notebookDescriptor = toNotebookDescriptor(mockNotebookDescriptor);
        return notebookDescriptor;
    }

    private NotebookDescriptor toNotebookDescriptor(MockNotebookDescriptor mockNotebookDescriptor) {
        return new NotebookDescriptor(mockNotebookDescriptor.getId(), mockNotebookDescriptor.getName(), mockNotebookDescriptor.getDescription(), mockNotebookDescriptor.getOwner(), new Date(), new Date());
    }

    @Override
    public NotebookDescriptor updateNotebook(Long aLong, String s, String s1) throws Exception {
        MockNotebookDescriptor mockNotebookDescriptor = entityManager.find(MockNotebookDescriptor.class, aLong);
        return toNotebookDescriptor(mockNotebookDescriptor);
    }

    @Override
    public List<NotebookDescriptor> listNotebooks(String s) throws Exception {
        TypedQuery<MockNotebookDescriptor> query = entityManager.createQuery("select o from MockNotebookDescriptor o", MockNotebookDescriptor.class);
        List<NotebookDescriptor> list = new ArrayList<>();
        for (MockNotebookDescriptor mockNotebookDescriptor : query.getResultList()) {
            list.add(toNotebookDescriptor(mockNotebookDescriptor));

        }
        return list;
    }

    @Override
    public List<NotebookEditable> listEditables(Long aLong, String s) throws Exception {
        TypedQuery<MockNotebookEditable> query = entityManager.createQuery("select o from MockNotebookEditable o where o.notebookId = :notebookId and o.userName = :userName", MockNotebookEditable.class);
        query.setParameter("notebookId", aLong);
        query.setParameter("userName", s);
        List<NotebookEditable> list = new ArrayList<>();
        for (MockNotebookEditable mockNotebookEditable : query.getResultList()) {
            list.add(toNotebookEditable(mockNotebookEditable));
        }
        return list;
    }

    private NotebookEditable toNotebookEditable(MockNotebookEditable mockNotebookEditable) {
        String jsonString = mockNotebookEditable.getJson() == null ? null : new String(mockNotebookEditable.getJson());
        return new NotebookEditable(mockNotebookEditable.getId(), mockNotebookEditable.getNotebookId(), null, mockNotebookEditable.getUserName(), new Date(), new Date(), jsonString);
    }

    @Override
    public NotebookEditable createEditable(Long aLong, Long aLong1, String s) throws Exception {
        MockNotebookEditable mockNotebookEditable = new MockNotebookEditable();
        mockNotebookEditable.setNotebookId(aLong);
        mockNotebookEditable.setUserName(s);
        entityManager.persist(mockNotebookEditable);
        return toNotebookEditable(mockNotebookEditable);
    }

    @Override
    public NotebookEditable updateEditable(Long aLong, Long aLong1, String s) throws Exception {
        MockNotebookEditable mockNotebookEditable = entityManager.find(MockNotebookEditable.class, aLong1);
        mockNotebookEditable.setNotebookId(aLong);
        mockNotebookEditable.setJson(s.getBytes());
        return  toNotebookEditable(mockNotebookEditable);
    }

    @Override
    public NotebookEditable createSavepoint(Long aLong, Long aLong1) throws Exception {
        return null;
    }

    @Override
    public List<NotebookSavepoint> listSavepoints(Long aLong) throws Exception {
        return null;
    }

    @Override
    public NotebookSavepoint setSavepointDescription(Long aLong, Long aLong1, String s) throws Exception {
        return null;
    }

    @Override
    public NotebookSavepoint setSavepointLabel(Long aLong, Long aLong1, String s) throws Exception {
        return null;
    }

    @Override
    public String readTextValue(Long aLong, Long aLong1, String s, String s1) throws Exception {
        TypedQuery<MockNotebookEditable> query = entityManager.createQuery("select o from MockNotebookEditable o where o.notebookId = :notebookId", MockNotebookEditable.class);
        query.setParameter("notebookId", aLong);
        MockNotebookEditable mockNotebookEditable = query.getResultList().get(0);
        NotebookInstance notebookInstance = NotebookInstance.fromJsonString(new String(mockNotebookEditable.getJson()));
        return (String)notebookInstance.findCellById(aLong1).getOutputVariableMap().get(s).getValue();
    }

    @Override
    public String readTextValue(Long aLong, String s, String s1, String s2) throws Exception {
        return null;
    }

    @Override
    public void writeTextValue(Long aLong, Long aLong1, Long aLong2, String s, String s1, String s2) throws Exception {
        TypedQuery<MockNotebookEditable> query = entityManager.createQuery("select o from MockNotebookEditable o where o.notebookId = :notebookId", MockNotebookEditable.class);
        query.setParameter("notebookId", aLong);
        MockNotebookEditable mockNotebookEditable = query.getResultList().get(0);
        NotebookInstance notebookInstance = NotebookInstance.fromJsonString(new String(mockNotebookEditable.getJson()));
        notebookInstance.findVariable(aLong2, s).setValue(s1);
        mockNotebookEditable.setJson(notebookInstance.toJsonString().getBytes());
    }

    @Override
    public InputStream readStreamValue(Long aLong, Long aLong1, String s, String s1) throws Exception {
        File file = resolveFile(aLong, aLong1, s);
        if (file.exists()) {
            return new FileInputStream(file);
        } else {
            return null;
        }
    }

    private File resolveFile(Long aLong, Long aLong1, String s) throws Exception {
        File root = new File(System.getProperty("user.home") + "/portal-files/" + aLong + "/" + aLong1);
        if (!root.exists() && !root.mkdirs()) {
            throw new IOException("Could not create " + root.getAbsolutePath());
        }
        File file = new File(root, URLEncoder.encode(s, "ISO-8859-1"));
        return file;
    }

    @Override
    public InputStream readStreamValue(Long aLong, String s, String s1, String s2) throws Exception {
         return null;
    }

    @Override
    public void writeStreamValue(Long aLong, Long aLong1, Long aLong2, String s, InputStream inputStream, String s1) throws Exception {
        File file = resolveFile(aLong, aLong2, s);
        FileOutputStream outputStream = new FileOutputStream(file);
        try {
            byte[] buffer = new byte[4096];
            int r = inputStream.read(buffer, 0, buffer.length);
            while (r > -1) {
                outputStream.write(buffer, 0, r);
                r = inputStream.read(buffer, 0, buffer.length);
            }
            outputStream.flush();
        } finally {
            outputStream.close();
        }
    }
}
