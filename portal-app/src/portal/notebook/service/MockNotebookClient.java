package portal.notebook.service;

import org.squonk.client.NotebookClient;
import org.squonk.notebook.api2.NotebookDescriptor;
import org.squonk.notebook.api2.NotebookEditable;
import org.squonk.notebook.api2.NotebookSavepoint;
import portal.notebook.api.CellInstance;
import portal.notebook.api.NotebookInstance;
import portal.notebook.api.VariableInstance;
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

    private NotebookSavepoint toNotebookSavepoint(MockNotebookSavepoint mockNotebookSavepoint) {
        String jsonString = mockNotebookSavepoint.getJson() == null ? null : new String(mockNotebookSavepoint.getJson());
        return new NotebookSavepoint(mockNotebookSavepoint.getId(), mockNotebookSavepoint.getNotebookId(), mockNotebookSavepoint.getEditableId(), null, null, null,  mockNotebookSavepoint.getDescription(), mockNotebookSavepoint.getDescription(), jsonString);
    }

    @Override
    public NotebookEditable createEditable(Long notebookId, Long aLong1, String userName) throws Exception {
        MockNotebookDescriptor mockNotebookDescriptor = entityManager.find(MockNotebookDescriptor.class, notebookId);
        if (mockNotebookDescriptor == null) {
            throw new RuntimeException("Unknown notebook id");
        }
        MockNotebookEditable mockNotebookEditable = new MockNotebookEditable();
        mockNotebookEditable.setNotebookId(notebookId);
        mockNotebookEditable.setUserName(userName);
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
    public NotebookEditable createSavepoint(Long notebookId, Long editableId) throws Exception {
        MockNotebookEditable mockNotebookEditable = entityManager.find(MockNotebookEditable.class, editableId);
        if (mockNotebookEditable == null) {
            throw new RuntimeException("Unknown editable id");
        }
        MockNotebookSavepoint mockNotebookSavepoint = new MockNotebookSavepoint();
        mockNotebookSavepoint.setEditableId(editableId);
        mockNotebookSavepoint.setNotebookId(notebookId);
        entityManager.persist(mockNotebookSavepoint);
        return toNotebookEditable(mockNotebookSavepoint);
    }

    @Override
    public List<NotebookSavepoint> listSavepoints(Long notebookId) throws Exception {
        TypedQuery<MockNotebookSavepoint> query = entityManager.createQuery("select o from MockNotebookSavepoint o where o.notebookId = :notebookId", MockNotebookSavepoint.class);
        query.setParameter("notebookId", notebookId);
        List<NotebookSavepoint> list = new ArrayList<>();
        for (MockNotebookSavepoint mockNotebookSavepoint : query.getResultList()) {
            list.add(toNotebookSavepoint(mockNotebookSavepoint));
        }
        return list;
    }

    @Override
    public NotebookSavepoint setSavepointDescription(Long notebookId, Long savepointId, String s) throws Exception {
        MockNotebookSavepoint mockNotebookSavepoint = entityManager.find(MockNotebookSavepoint.class, savepointId);
        mockNotebookSavepoint.setDescription(s);
        return toNotebookSavepoint(mockNotebookSavepoint);
    }

    @Override
    public NotebookSavepoint setSavepointLabel(Long notebookId, Long savepointId, String s) throws Exception {
        MockNotebookSavepoint mockNotebookSavepoint = entityManager.find(MockNotebookSavepoint.class, savepointId);
        mockNotebookSavepoint.setLabel(s);
        return toNotebookSavepoint(mockNotebookSavepoint);
    }

    @Override
    public String readTextValue(Long notebookId, Long editableId, String name, String key) throws Exception {
        MockVariable mockVariable = findMockVariable(editableId, name);
        return mockVariable == null ? null : mockVariable.getValue();
    }

    @Override
    public String readTextValue(Long aLong, String s, String s1, String s2) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTextValue(Long notebookId, Long editableId, Long cellId, String name, String value, String s2) throws Exception {
        MockVariable mockVariable = findMockVariable(editableId, name);
        if (mockVariable == null) {
            TypedQuery<MockNotebookEditable> editableQuery = entityManager.createQuery("select o from MockNotebookEditable o where o.notebookId = :notebookId", MockNotebookEditable.class);
            editableQuery.setParameter("notebookId", notebookId);
            MockNotebookEditable mockNotebookEditable = editableQuery.getResultList().get(0);
            mockVariable = new MockVariable();
            mockVariable.setMockNotebookEditable(mockNotebookEditable);
            mockVariable.setCellId(cellId);
            mockVariable.setName(name);
            entityManager.persist(mockVariable);
        }
        mockVariable.setValue(value);
    }

    @Override
    public InputStream readStreamValue(Long notebookId, Long sourceId, String name, String s1) throws Exception {
        MockVariable mockVariable = findMockVariable(sourceId, name);
        byte[] bytes = mockVariable == null ? null : mockVariable.getStreamValue();
        if (bytes == null) {
            return null;
        } else {
            return new ByteArrayInputStream(bytes);
        }
    }

    private File resolveFile(Long notebookId, Long cellId, String variableName) throws Exception {
        File root = new File(System.getProperty("user.home") + "/portal-files/" + notebookId + "/" + cellId);
        if (!root.exists() && !root.mkdirs()) {
            throw new IOException("Could not create " + root.getAbsolutePath());
        }
        File file = new File(root, URLEncoder.encode(variableName, "ISO-8859-1"));
        return file;
    }

    @Override
    public InputStream readStreamValue(Long aLong, String s, String s1, String s2) throws Exception {
         return null;
    }

    @Override
    public void writeStreamValue(Long notebookId, Long editableId, Long cellId, String name, InputStream inputStream, String s1) throws Exception {
        MockVariable mockVariable = findMockVariable(editableId, name);
        if (mockVariable == null) {
            TypedQuery<MockNotebookEditable> editableQuery = entityManager.createQuery("select o from MockNotebookEditable o where o.notebookId = :notebookId", MockNotebookEditable.class);
            editableQuery.setParameter("notebookId", notebookId);
            MockNotebookEditable mockNotebookEditable = editableQuery.getResultList().get(0);
            mockVariable = new MockVariable();
            mockVariable.setMockNotebookEditable(mockNotebookEditable);
            mockVariable.setCellId(cellId);
            mockVariable.setName(name);
            entityManager.persist(mockVariable);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int r = inputStream.read(buffer, 0, buffer.length);
        while (r > -1) {
            byteArrayOutputStream.write(buffer, 0, r);
            r = inputStream.read(buffer, 0, buffer.length);
        }
        byteArrayOutputStream.flush();
        mockVariable.setStreamValue(byteArrayOutputStream.toByteArray());
    }

    private MockVariable findMockVariable(Long editableId, String name) {
        TypedQuery<MockVariable> variableQuery = entityManager.createQuery("select o from MockVariable o where o.mockNotebookEditable.id = :editableId and o.name = :name", MockVariable.class);
        variableQuery.setParameter("editableId", editableId);
        variableQuery.setParameter("name", name);
        return variableQuery.getResultList().isEmpty() ? null : variableQuery.getResultList().get(0);
    }

    private MockNotebookEditable findMockNotebookEditableByNotebookId(Long notebookId) {
        TypedQuery<MockNotebookEditable> query = entityManager.createQuery("select o from MockNotebookEditable o where o.notebookId = :notebookId", MockNotebookEditable.class);
        query.setParameter("notebookId", notebookId);
        return query.getResultList().isEmpty() ? null : query.getResultList().get(0);
    }

    public void oldWriteStreamValue(Long notebookId, String cellName, String name, InputStream dataInputStream) {
        try {
            MockNotebookEditable mockNotebookEditable = findMockNotebookEditableByNotebookId(notebookId);
            NotebookInstance notebookInstance = NotebookInstance.fromJsonString(new String(mockNotebookEditable.getJson()));
            VariableInstance variableInstance = notebookInstance.findVariableByCellName(cellName, name);
            writeStreamValue(notebookId, null, variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), dataInputStream, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void oldWriteTextValue(Long notebookId, String cellName, String name, String value) {
        try {
            MockNotebookEditable mockNotebookEditable = findMockNotebookEditableByNotebookId(notebookId);
            NotebookInstance notebookInstance = NotebookInstance.fromJsonString(new String(mockNotebookEditable.getJson()));
            CellInstance cellInstance = notebookInstance.findCellInstanceByName(cellName);
            writeTextValue(notebookId, mockNotebookEditable.getId(), cellInstance.getId(), name, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String oldReadTextValue(Long notebookId, String cellName, String name) {
        try {
            MockNotebookEditable mockNotebookEditable = findMockNotebookEditableByNotebookId(notebookId);
            return readTextValue(notebookId, mockNotebookEditable.getId(), name, "DEFAULT");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CellInstance oldFindCellInstance(Long notebookId, String cellName) {
        try {
            MockNotebookEditable mockNotebookEditable = findMockNotebookEditableByNotebookId(notebookId);
            NotebookInstance notebookInstance = NotebookInstance.fromJsonString(new String(mockNotebookEditable.getJson()));
            return notebookInstance.findCellInstanceByName(cellName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
