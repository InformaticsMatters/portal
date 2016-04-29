package portal.notebook.service;

import org.squonk.client.NotebookVariableClient;
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
        MockNotebook mockNotebook = new MockNotebook();
        mockNotebook.setOwner(owner);
        mockNotebook.setName(name);
        mockNotebook.setDescription(desc);
        entityManager.persist(mockNotebook);
        NotebookDTO notebookDTO = toNotebookDTO(mockNotebook);
        return notebookDTO;
    }

    @Override
    public boolean deleteNotebook(Long notebookId) throws Exception {
        MockNotebook mockNotebook = entityManager.find(MockNotebook.class, notebookId);
        if (mockNotebook == null) {
            return false;
        } else {
            TypedQuery<MockNotebookEditable> editableQuery = entityManager.createQuery("select o from MockNotebookEditable o where o.mockNotebook.id = :notebookId", MockNotebookEditable.class);
            TypedQuery<MockVariable> variableQuery = entityManager.createQuery("select o from MockVariable o where o.mockNotebookEditable.id = :editableId", MockVariable.class);
            editableQuery.setParameter("notebookId", notebookId);
            for (MockNotebookEditable mockNotebookEditable : editableQuery.getResultList()) {
                variableQuery.setParameter("editableId", mockNotebookEditable.getId());
                for (MockVariable mockVariable : variableQuery.getResultList()) {
                    entityManager.remove(mockVariable);
                }
                entityManager.remove(mockNotebookEditable);
            }
            entityManager.remove(mockNotebook);
            return true;
        }
    }

    private NotebookDTO toNotebookDTO(MockNotebook mockNotebook) {
        TypedQuery<MockNotebookNotebookLayer> layerQuery = entityManager.createQuery("select o from MockNotebookNotebookLayer o where o.mockNotebook = :mockNotebook", MockNotebookNotebookLayer.class);
        layerQuery.setParameter("mockNotebook", mockNotebook);
        NotebookDTO notebookDTO = new NotebookDTO(mockNotebook.getId(), mockNotebook.getName(), mockNotebook.getDescription(), mockNotebook.getOwner(), new Date(), new Date(), null);
        for (MockNotebookNotebookLayer mockNotebookNotebookLayer : layerQuery.getResultList()) {
            notebookDTO.getLayers().add(mockNotebookNotebookLayer.getLayerName());
        }
        return notebookDTO;
    }

    @Override
    public NotebookDTO updateNotebook(Long aLong, String s, String s1) throws Exception {
        MockNotebook mockNotebook = entityManager.find(MockNotebook.class, aLong);
        return toNotebookDTO(mockNotebook);
    }

    @Override
    public NotebookDTO addNotebookToLayer(Long aLong, String layerName) throws Exception {
        MockNotebook mockNotebook = entityManager.find(MockNotebook.class, aLong);
        MockNotebookNotebookLayer mockNotebookNotebookLayer = new MockNotebookNotebookLayer();
        mockNotebookNotebookLayer.setMockNotebook(mockNotebook);
        mockNotebookNotebookLayer.setLayerName(layerName);
        entityManager.persist(mockNotebookNotebookLayer);
        NotebookDTO notebookDTO = toNotebookDTO(mockNotebook);
        return notebookDTO;
    }

    @Override
    public NotebookDTO removeNotebookFromLayer(Long aLong, String s) throws Exception {
        TypedQuery<MockNotebookNotebookLayer> query = entityManager.createQuery("select o from MockNotebookNotebookLayer o where o.mockNotebook.id = :notebookId and o.layerName = :layerName", MockNotebookNotebookLayer.class);
        query.setParameter("notebookId", aLong);
        query.setParameter("layerName", s);
        if (!query.getResultList().isEmpty()) {
            entityManager.remove(query.getResultList().get(0));
        }
        return toNotebookDTO(entityManager.find(MockNotebook.class, aLong));
    }

    @Override
    public List<String> listLayers(Long aLong) throws Exception {
        TypedQuery<MockNotebookNotebookLayer> query = entityManager.createQuery("select o from MockNotebookNotebookLayer o where o.mockNotebook.id = :notebookId order by o.layerName", MockNotebookNotebookLayer.class);
        query.setParameter("notebookId", aLong);
        List<String> list = new ArrayList<>();
        for (MockNotebookNotebookLayer mockNotebookNotebookLayer : query.getResultList()) {
            list.add(mockNotebookNotebookLayer.getLayerName());
        }
        return list;
    }

    @Override
    public List<NotebookDTO> listNotebooks(String userName) throws Exception {
        TypedQuery<MockNotebook> query = entityManager.createQuery("select o from MockNotebook o", MockNotebook.class);
        List<NotebookDTO> list = new ArrayList<>();
        for (MockNotebook mockNotebook : query.getResultList()) {
            list.add(toNotebookDTO(mockNotebook));

        }
        return list;
    }

    @Override
    public List<NotebookEditableDTO> listEditables(Long aLong, String userName) throws Exception {
        TypedQuery<MockNotebookEditable> query = entityManager.createQuery("select o from MockNotebookEditable o where o.mockNotebook.id = :notebookId and o.owner = :userName order by o.id", MockNotebookEditable.class);
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
        Long parentId = mockNotebookEditable.getParent() == null ? null : mockNotebookEditable.getParent().getId();
        return new NotebookEditableDTO(mockNotebookEditable.getId(), mockNotebookEditable.getMockNotebook().getId(), parentId, mockNotebookEditable.getOwner(), mockNotebookEditable.getCreatedDate(), mockNotebookEditable.getLastUpdatedDate(), dto);
    }

    @Override
    public NotebookEditableDTO createEditable(Long notebookId, Long parentId, String userName) throws Exception {
        MockNotebook mockNotebook = entityManager.find(MockNotebook.class, notebookId);
        if (mockNotebook == null) {
            throw new RuntimeException("Unknown notebook id");
        }
        MockNotebookEditable parent = parentId == null ? null : entityManager.find(MockNotebookEditable.class, parentId);
        MockNotebookEditable mockNotebookEditable = new MockNotebookEditable();
        mockNotebookEditable.setParent(parent);
        mockNotebookEditable.setMockNotebook(mockNotebook);
        mockNotebookEditable.setOwner(userName);
        mockNotebookEditable.setCreatedDate(new Date());
        mockNotebookEditable.setLastUpdatedDate(mockNotebookEditable.getCreatedDate());
        entityManager.persist(mockNotebookEditable);
        return toNotebookEditable(mockNotebookEditable);
    }

    @Override
    public NotebookEditableDTO updateEditable(Long notebookId, Long aLong1, NotebookCanvasDTO canvasDTO) throws Exception {
        MockNotebook mockNotebook = entityManager.find(MockNotebook.class, notebookId);
        MockNotebookEditable mockNotebookEditable = entityManager.find(MockNotebookEditable.class, aLong1);
        mockNotebookEditable.setMockNotebook(mockNotebook);
        String json = JsonHandler.getInstance().objectToJson(canvasDTO);
        mockNotebookEditable.setJson(json.getBytes());
        mockNotebookEditable.setLastUpdatedDate(new Date());
        return toNotebookEditable(mockNotebookEditable);
    }

    @Override
    public NotebookEditableDTO createSavepoint(Long notebookId, Long editableId) throws Exception {
        MockNotebookEditable mockNotebookEditable = entityManager.find(MockNotebookEditable.class, editableId);
        if (mockNotebookEditable == null) {
            throw new RuntimeException("Unknown editable id");
        }
        MockNotebookSavepoint mockNotebookSavepoint = new MockNotebookSavepoint();
        mockNotebookSavepoint.setParent(mockNotebookEditable);
        mockNotebookSavepoint.setMockNotebook(mockNotebookEditable.getMockNotebook());
        mockNotebookSavepoint.setCreatedDate(new Date());
        mockNotebookSavepoint.setLastUpdatedDate(mockNotebookSavepoint.getCreatedDate());
        entityManager.persist(mockNotebookSavepoint);
        NotebookEditableDTO notebookEditableDTO = new NotebookEditableDTO(mockNotebookSavepoint.getId(), mockNotebookEditable.getMockNotebook().getId(), mockNotebookEditable.getId(), mockNotebookEditable.getOwner(), new Date(), new Date(), null);
        return notebookEditableDTO;
    }

    @Override
    public List<NotebookSavepointDTO> listSavepoints(Long notebookId) throws Exception {
        TypedQuery<MockNotebookSavepoint> query = entityManager.createQuery("select o from MockNotebookSavepoint o where o.mockNotebook.id = :notebookId", MockNotebookSavepoint.class);
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

        return new NotebookSavepointDTO(mockNotebookSavepoint.getId(), mockNotebookSavepoint.getMockNotebook().getId(), mockNotebookSavepoint.getParent().getId(), mockNotebookSavepoint.getParent().getOwner(), mockNotebookSavepoint.getCreatedDate(), mockNotebookSavepoint.getLastUpdatedDate(),  mockNotebookSavepoint.getDescription(), mockNotebookSavepoint.getDescription(), dto);
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
            TypedQuery<MockNotebookEditable> editableQuery = entityManager.createQuery("select o from MockNotebookEditable o where o.mockNotebook.id = :notebookId", MockNotebookEditable.class);
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
            TypedQuery<MockNotebookEditable> editableQuery = entityManager.createQuery("select o from MockNotebookEditable o where o.mockNotebook.id = :notebookId", MockNotebookEditable.class);
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


}
