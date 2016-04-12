package portal.notebook.service;

import org.squonk.client.NotebookClient;
import org.squonk.client.NotebookVariableClient;
import org.squonk.client.VariableClient;
import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.notebook.api.NotebookDTO;
import org.squonk.notebook.api.NotebookEditableDTO;
import org.squonk.notebook.api.NotebookSavepointDTO;
import org.squonk.types.io.JsonHandler;
import toolkit.services.PU;
import toolkit.services.Transactional;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Alternative
@RequestScoped
@Transactional
public class MockNotebookVariableClient implements NotebookVariableClient {
    @Inject
    @PU(puName = PortalConstants.PU_NAME)
    private EntityManager entityManager;

    @Override
    public NotebookDTO createNotebook(String owner, String name, String desc) throws Exception {
        MockNotebookDescriptor mockNotebookDescriptor = new MockNotebookDescriptor();
        mockNotebookDescriptor.setOwner(owner);
        mockNotebookDescriptor.setName(name);
        mockNotebookDescriptor.setDescription(desc);
        entityManager.persist(mockNotebookDescriptor);
        System.out.println("Created new empty notebook");
        NotebookDTO notebookDescriptor = toNotebookDescriptor(mockNotebookDescriptor);
        return notebookDescriptor;
    }

    @Override
    public boolean deleteNotebook(Long aLong) throws Exception {
        throw new UnsupportedOperationException("NYI");
    }

    private NotebookDTO toNotebookDescriptor(MockNotebookDescriptor mockNotebookDescriptor) {
        return new NotebookDTO(mockNotebookDescriptor.getId(), mockNotebookDescriptor.getName(), mockNotebookDescriptor.getDescription(), mockNotebookDescriptor.getOwner(), new Date(), new Date());
    }

    @Override
    public NotebookDTO updateNotebook(Long aLong, String s, String s1) throws Exception {
        MockNotebookDescriptor mockNotebookDescriptor = entityManager.find(MockNotebookDescriptor.class, aLong);
        return toNotebookDescriptor(mockNotebookDescriptor);
    }

    @Override
    public void addNotebookToLayer(Long aLong, String s) throws Exception {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public void removeNotebookFromLayer(Long aLong, String s) throws Exception {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public List<String> listLayers(Long aLong) throws Exception {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public List<NotebookDTO> listNotebooks(String userName) throws Exception {
        TypedQuery<MockNotebookDescriptor> query = entityManager.createQuery("select o from MockNotebookDescriptor o", MockNotebookDescriptor.class);
        List<NotebookDTO> list = new ArrayList<>();
        for (MockNotebookDescriptor mockNotebookDescriptor : query.getResultList()) {
            list.add(toNotebookDescriptor(mockNotebookDescriptor));

        }
        return list;
    }

    @Override
    public List<NotebookEditableDTO> listEditables(Long aLong, String userName) throws Exception {
        TypedQuery<MockNotebookEditable> query = entityManager.createQuery("select o from MockNotebookEditable o where o.notebookId = :notebookId and o.userName = :userName order by o.id", MockNotebookEditable.class);
        query.setParameter("notebookId", aLong);
        query.setParameter("userName", userName);
        List<NotebookEditableDTO> list = new ArrayList<>();
        for (MockNotebookEditable mockNotebookEditable : query.getResultList()) {
            list.add(toNotebookEditable(mockNotebookEditable));
        }
        return list;
    }

    private NotebookEditableDTO toNotebookEditable(MockNotebookEditable mockNotebookEditable) throws Exception {
        String jsonString = mockNotebookEditable.getJson() == null ? null : new String(mockNotebookEditable.getJson());
        NotebookCanvasDTO dto = (jsonString == null ? null : JsonHandler.getInstance().objectFromJson(jsonString, NotebookCanvasDTO.class));
        return new NotebookEditableDTO(mockNotebookEditable.getId(), mockNotebookEditable.getNotebookId(), null, mockNotebookEditable.getUserName(), new Date(), new Date(), dto);
    }

    @Override
    public NotebookEditableDTO createEditable(Long notebookId, Long aLong1, String userName) throws Exception {
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
    public NotebookEditableDTO updateEditable(Long aLong, Long aLong1, NotebookCanvasDTO canvasDTO) throws Exception {
        MockNotebookEditable mockNotebookEditable = entityManager.find(MockNotebookEditable.class, aLong1);
        mockNotebookEditable.setNotebookId(aLong);
        String json = JsonHandler.getInstance().objectToJson(canvasDTO);
        mockNotebookEditable.setJson(json.getBytes());
        return toNotebookEditable(mockNotebookEditable);
    }

    @Override
    public NotebookEditableDTO createSavepoint(Long notebookId, Long editableId) throws Exception {
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
    public List<NotebookSavepointDTO> listSavepoints(Long notebookId) throws Exception {
        TypedQuery<MockNotebookSavepoint> query = entityManager.createQuery("select o from MockNotebookSavepoint o where o.notebookId = :notebookId", MockNotebookSavepoint.class);
        query.setParameter("notebookId", notebookId);
        List<NotebookSavepointDTO> list = new ArrayList<>();
        for (MockNotebookSavepoint mockNotebookSavepoint : query.getResultList()) {
            list.add(toNotebookSavepoint(mockNotebookSavepoint));
        }
        return list;
    }

    @Override
    public NotebookSavepointDTO setSavepointDescription(Long notebookId, Long savepointId, String desc) throws Exception {
        MockNotebookSavepoint mockNotebookSavepoint = entityManager.find(MockNotebookSavepoint.class, savepointId);
        mockNotebookSavepoint.setDescription(desc);
        return toNotebookSavepoint(mockNotebookSavepoint);
    }

    @Override
    public NotebookSavepointDTO setSavepointLabel(Long notebookId, Long savepointId, String label) throws Exception {
        MockNotebookSavepoint mockNotebookSavepoint = entityManager.find(MockNotebookSavepoint.class, savepointId);
        mockNotebookSavepoint.setLabel(label);
        return toNotebookSavepoint(mockNotebookSavepoint);
    }

    private NotebookSavepointDTO toNotebookSavepoint(MockNotebookSavepoint mockNotebookSavepoint) {
        String jsonString = mockNotebookSavepoint.getJson() == null ? null : new String(mockNotebookSavepoint.getJson());
        NotebookCanvasDTO dto = null;
        try {
            dto = (jsonString == null ? null : JsonHandler.getInstance().objectFromJson(jsonString, NotebookCanvasDTO.class));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to unmarshal canvas dto", e);
        }

        return new NotebookSavepointDTO(mockNotebookSavepoint.getId(), mockNotebookSavepoint.getNotebookId(), mockNotebookSavepoint.getEditableId(), null, null, null,  mockNotebookSavepoint.getDescription(), mockNotebookSavepoint.getDescription(), dto);
    }


    @Override
    public String readTextValue(Long notebookId, Long editableId, Long cellId, String name, String key) throws Exception {
        MockVariable mockVariable = findMockVariable(editableId, cellId, name);
        return mockVariable == null ? null : mockVariable.getValue();
    }

    @Override
    public void writeTextValue(Long notebookId, Long editableId, Long cellId, String name, String value, String key) throws Exception {
        MockVariable mockVariable = findMockVariable(editableId, cellId, name);
        if (mockVariable == null) {
            TypedQuery<MockNotebookEditable> editableQuery = entityManager.createQuery("select o from MockNotebookEditable o where o.notebookId = :notebookId", MockNotebookEditable.class);
            editableQuery.setParameter("notebookId", notebookId);
            MockNotebookEditable mockNotebookEditable = entityManager.find(MockNotebookEditable.class, editableId);
            mockVariable = new MockVariable();
            mockVariable.setMockNotebookEditable(mockNotebookEditable);
            mockVariable.setCellId(cellId);
            mockVariable.setName(name);
            entityManager.persist(mockVariable);
        }
        mockVariable.setValue(value);
    }

    @Override
    public InputStream readStreamValue(Long notebookId, Long sourceId, Long cellId, String name, String key) throws Exception {
        MockVariable mockVariable = findMockVariable(sourceId, cellId, name);
        byte[] bytes = mockVariable == null ? null : mockVariable.getStreamValue();
        if (bytes == null) {
            return null;
        } else {
            return new ByteArrayInputStream(bytes);
        }
    }

    @Override
    public void writeStreamValue(Long notebookId, Long editableId, Long cellId, String name, InputStream inputStream, String s1) throws Exception {
        MockVariable mockVariable = findMockVariable(editableId, cellId, name);
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

    private MockVariable findMockVariable(Long editableId, Long cellId, String name) {
        TypedQuery<MockVariable> variableQuery = entityManager.createQuery("select o from MockVariable o where o.mockNotebookEditable.id = :editableId and o.cellId = :cellId and o.name = :name", MockVariable.class);
        variableQuery.setParameter("editableId", editableId);
        variableQuery.setParameter("cellId", cellId);
        variableQuery.setParameter("name", name);
        return variableQuery.getResultList().isEmpty() ? null : variableQuery.getResultList().get(0);
    }

    private MockNotebookEditable findMockNotebookEditableByNotebookId(Long notebookId) {
        TypedQuery<MockNotebookEditable> query = entityManager.createQuery("select o from MockNotebookEditable o where o.notebookId = :notebookId", MockNotebookEditable.class);
        query.setParameter("notebookId", notebookId);
        return query.getResultList().isEmpty() ? null : query.getResultList().get(0);
    }


    public NotebookCanvasDTO.CellDTO findCell(Long notebookId, Long cellId) {
        try {
            MockNotebookEditable mockNotebookEditable = findMockNotebookEditableByNotebookId(notebookId);
            NotebookCanvasDTO notebookCanvasDTO = JsonHandler.getInstance().objectFromJson(new String(mockNotebookEditable.getJson()), NotebookCanvasDTO.class);
            for (NotebookCanvasDTO.CellDTO cellDTO : notebookCanvasDTO.getCells()) {
                if (cellDTO.getId().equals(cellId)) {
                    return cellDTO;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}