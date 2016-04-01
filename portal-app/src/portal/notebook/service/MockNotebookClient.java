package portal.notebook.service;

import org.squonk.client.NotebookVariableClient;
import org.squonk.notebook.api.*;
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
public class MockNotebookClient implements NotebookVariableClient {
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

    private NotebookEditable toNotebookEditable(MockNotebookEditable mockNotebookEditable) throws Exception {
        String jsonString = mockNotebookEditable.getJson() == null ? null : new String(mockNotebookEditable.getJson());
        return new NotebookEditable(mockNotebookEditable.getId(), mockNotebookEditable.getNotebookId(), null, mockNotebookEditable.getUserName(), new Date(), new Date(),
                NotebookInstance.fromJsonString(jsonString));
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
    public NotebookEditable updateEditable(Long aLong, Long aLong1, NotebookInstance nbInstance) throws Exception {
        MockNotebookEditable mockNotebookEditable = entityManager.find(MockNotebookEditable.class, aLong1);
        mockNotebookEditable.setNotebookId(aLong);
        mockNotebookEditable.setJson(nbInstance.toJsonString().getBytes());
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

    public void oldWriteStreamValue(Long notebookId, Long cellId, String name, InputStream dataInputStream) {
        try {
            MockNotebookEditable mockNotebookEditable = findMockNotebookEditableByNotebookId(notebookId);
            NotebookInstance notebookInstance = NotebookInstance.fromJsonString(new String(mockNotebookEditable.getJson()));
            VariableInstance variableInstance = notebookInstance.findVariableByCellId(cellId, name);
            writeStreamValue(notebookId, null, variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), dataInputStream, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void oldWriteTextValue(Long notebookId, Long cellId, String name, String value) {
        try {
            MockNotebookEditable mockNotebookEditable = findMockNotebookEditableByNotebookId(notebookId);
            NotebookInstance notebookInstance = NotebookInstance.fromJsonString(new String(mockNotebookEditable.getJson()));
            CellInstance cellInstance = notebookInstance.findCellInstanceById(cellId);
            writeTextValue(notebookId, mockNotebookEditable.getId(), cellInstance.getId(), name, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String oldReadTextValue(Long notebookId, Long cellId, String name) {
        try {
            MockNotebookEditable mockNotebookEditable = findMockNotebookEditableByNotebookId(notebookId);
            return readTextValue(notebookId, mockNotebookEditable.getId(), name, "DEFAULT");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CellInstance oldFindCellInstance(Long notebookId, Long cellId) {
        try {
            MockNotebookEditable mockNotebookEditable = findMockNotebookEditableByNotebookId(notebookId);
            NotebookInstance notebookInstance = NotebookInstance.fromJsonString(new String(mockNotebookEditable.getJson()));
            return notebookInstance.findCellInstanceById(cellId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
