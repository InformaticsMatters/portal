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
import java.util.logging.Logger;

@Alternative
@RequestScoped
@Transactional
public class MockNotebookVariableClient implements NotebookVariableClient {
    private static final Logger LOGGER = Logger.getLogger(MockNotebookVariableClient.class.getName());
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
        createEditable(mockNotebook.getId(), null, owner);
        NotebookDTO notebookDTO = toNotebookDTO(mockNotebook);
        return notebookDTO;
    }

    @Override
    public boolean deleteNotebook(Long notebookId) throws Exception {
        MockNotebook mockNotebook = entityManager.find(MockNotebook.class, notebookId);
        if (mockNotebook == null) {
            return false;
        } else {
            TypedQuery<MockNotebookVersion> editableQuery = entityManager.createQuery("select o from MockNotebookVersion o where o.mockNotebook.id = :notebookId", MockNotebookVersion.class);
            TypedQuery<MockVariable> variableQuery = entityManager.createQuery("select o from MockVariable o where o.mockNotebookVersion.id = :editableId", MockVariable.class);
            editableQuery.setParameter("notebookId", notebookId);
            for (MockNotebookVersion mockNotebookVersion : editableQuery.getResultList()) {
                variableQuery.setParameter("editableId", mockNotebookVersion.getId());
                for (MockVariable mockVariable : variableQuery.getResultList()) {
                    entityManager.remove(mockVariable);
                }
                entityManager.remove(mockNotebookVersion);
            }
            entityManager.remove(mockNotebook);
            return true;
        }
    }

    private NotebookDTO toNotebookDTO(MockNotebook mockNotebook) {
        TypedQuery<MockNotebookNotebookLayer> layerQuery = entityManager.createQuery("select o from MockNotebookNotebookLayer o where o.mockNotebook = :mockNotebook", MockNotebookNotebookLayer.class);
        TypedQuery<MockNotebookSavepoint> savepointQuery = entityManager.createQuery("select o from MockNotebookSavepoint o where o.mockNotebookVersion.mockNotebook = :mockNotebook", MockNotebookSavepoint.class);
        layerQuery.setParameter("mockNotebook", mockNotebook);
        savepointQuery.setParameter("mockNotebook", mockNotebook);
        NotebookDTO notebookDTO = new NotebookDTO(mockNotebook.getId(), mockNotebook.getName(), mockNotebook.getDescription(), mockNotebook.getOwner(), new Date(), new Date(), null, savepointQuery.getResultList().size(), 0, 0);
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
            NotebookDTO notebookDTO = toNotebookDTO(mockNotebook);
            if (mockNotebook.getOwner().equals(userName) || (notebookDTO.getLayers().contains("public") && notebookDTO.getSavepointCount() > 0)) {
                list.add(toNotebookDTO(mockNotebook));
            }
        }
        return list;
    }


    @Override
    public List<NotebookEditableDTO> listEditables(Long aLong, String userName) throws Exception {
        TypedQuery<MockNotebookVersion> query = entityManager.createQuery("select o from MockNotebookVersion o where o.mockNotebook.id = :notebookId and o.owner = :userName and o.editable = :editable order by o.id", MockNotebookVersion.class);
        query.setParameter("notebookId", aLong);
        query.setParameter("userName", userName);
        query.setParameter("editable", Boolean.TRUE);
        List<NotebookEditableDTO> list = new ArrayList<>();
        for (MockNotebookVersion mockNotebookVersion : query.getResultList()) {
            list.add(toNotebookEditable(mockNotebookVersion));
        }
        return list;
    }

    private NotebookEditableDTO toNotebookEditable(MockNotebookVersion mockNotebookVersion) throws Exception {
        String jsonString = mockNotebookVersion.getJson() == null ? null : new String(mockNotebookVersion.getJson());
        NotebookCanvasDTO dto = (jsonString == null ? null : JsonHandler.getInstance().objectFromJson(jsonString, NotebookCanvasDTO.class));
        Long parentId = mockNotebookVersion.getParent() == null ? null : mockNotebookVersion.getParent().getId();
        return new NotebookEditableDTO(mockNotebookVersion.getId(), mockNotebookVersion.getMockNotebook().getId(), parentId, mockNotebookVersion.getOwner(), mockNotebookVersion.getCreatedDate(), mockNotebookVersion.getLastUpdatedDate(), dto);
    }

    @Override
    public NotebookEditableDTO createEditable(Long notebookId, Long parentId, String userName) throws Exception {
        MockNotebook mockNotebook = entityManager.find(MockNotebook.class, notebookId);
        if (mockNotebook == null) {
            throw new RuntimeException("Unknown notebook id");
        }
        MockNotebookVersion parent = parentId == null ? null : entityManager.find(MockNotebookVersion.class, parentId);
        MockNotebookVersion mockNotebookVersion = new MockNotebookVersion();
        mockNotebookVersion.setParent(parent);
        mockNotebookVersion.setMockNotebook(mockNotebook);
        mockNotebookVersion.setOwner(userName);
        mockNotebookVersion.setCreatedDate(new Date());
        mockNotebookVersion.setLastUpdatedDate(mockNotebookVersion.getCreatedDate());
        mockNotebookVersion.setEditable(Boolean.TRUE);
        if (parent == null) {
            String json = JsonHandler.getInstance().objectToJson(new NotebookCanvasDTO(0l));
            mockNotebookVersion.setJson(json.getBytes());
        } else {
            mockNotebookVersion.setJson(parent.getJson());
        }
        entityManager.persist(mockNotebookVersion);
        return toNotebookEditable(mockNotebookVersion);
    }

    @Override
    public NotebookEditableDTO updateEditable(Long notebookId, Long aLong1, NotebookCanvasDTO canvasDTO) throws Exception {
        MockNotebook mockNotebook = entityManager.find(MockNotebook.class, notebookId);
        MockNotebookVersion mockNotebookVersion = entityManager.find(MockNotebookVersion.class, aLong1);
        mockNotebookVersion.setMockNotebook(mockNotebook);
        String json = JsonHandler.getInstance().objectToJson(canvasDTO);
        mockNotebookVersion.setJson(json.getBytes());
        mockNotebookVersion.setLastUpdatedDate(new Date());
        return toNotebookEditable(mockNotebookVersion);
    }

    @Override
    public boolean deleteEditable(Long notebookId, Long editableId, String username) throws Exception {
        MockNotebookVersion mockNotebookVersion = entityManager.find(MockNotebookVersion.class, editableId);
        if (mockNotebookVersion == null) {
            return false;
        } else {
            entityManager.remove(mockNotebookVersion);
            return true;
        }
    }

    @Override
    public NotebookEditableDTO createSavepoint(Long notebookId, Long editableId, String description) throws Exception {
        MockNotebookVersion mockNotebookVersion = entityManager.find(MockNotebookVersion.class, editableId);
        if (mockNotebookVersion == null) {
            throw new RuntimeException("Unknown editable id");
        }
        mockNotebookVersion.setEditable(Boolean.FALSE);
        MockNotebookSavepoint mockNotebookSavepoint = new MockNotebookSavepoint();
        mockNotebookSavepoint.setCreator(mockNotebookVersion.getOwner());
        mockNotebookSavepoint.setMockNotebookVersion(mockNotebookVersion);
        mockNotebookSavepoint.setDescription(description);
        mockNotebookSavepoint.setLabel("");
        entityManager.persist(mockNotebookSavepoint);
        return createEditable(notebookId, mockNotebookVersion.getId(), mockNotebookVersion.getOwner());
    }

    @Override
    public List<NotebookSavepointDTO> listSavepoints(Long notebookId) throws Exception {
        TypedQuery<MockNotebookSavepoint> query = entityManager.createQuery("select o from MockNotebookSavepoint o where o.mockNotebookVersion.mockNotebook.id = :notebookId", MockNotebookSavepoint.class);
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

    private NotebookSavepointDTO toNotebookSavepoint(MockNotebookSavepoint mockNotebookSavepoint) throws IOException {
        MockNotebookVersion mockNotebookVersion = mockNotebookSavepoint.getMockNotebookVersion();
        String jsonString = mockNotebookVersion.getJson() == null ? null : new String(mockNotebookVersion.getJson());
        NotebookCanvasDTO dto = jsonString == null ? null : JsonHandler.getInstance().objectFromJson(jsonString, NotebookCanvasDTO.class);
        Long parentId = mockNotebookVersion.getParent() == null ? null : mockNotebookVersion.getParent().getId();
        return new NotebookSavepointDTO(mockNotebookVersion.getId(), mockNotebookVersion.getMockNotebook().getId(), parentId, mockNotebookSavepoint.getCreator(), mockNotebookVersion.getCreatedDate(), mockNotebookVersion.getLastUpdatedDate(),  mockNotebookSavepoint.getDescription(), mockNotebookSavepoint.getDescription(), dto);
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
            TypedQuery<MockNotebookVersion> editableQuery = entityManager.createQuery("select o from MockNotebookVersion o where o.mockNotebook.id = :notebookId", MockNotebookVersion.class);
            editableQuery.setParameter("notebookId", notebookId);
            MockNotebookVersion mockNotebookVersion = entityManager.find(MockNotebookVersion.class, editableId);
            mockVariable = new MockVariable();
            mockVariable.setMockNotebookVersion(mockNotebookVersion);
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
            TypedQuery<MockNotebookVersion> editableQuery = entityManager.createQuery("select o from MockNotebookVersion o where o.mockNotebook.id = :notebookId", MockNotebookVersion.class);
            editableQuery.setParameter("notebookId", notebookId);
            MockNotebookVersion mockNotebookVersion = editableQuery.getResultList().get(0);
            mockVariable = new MockVariable();
            mockVariable.setMockNotebookVersion(mockNotebookVersion);
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
        TypedQuery<MockVariable> variableQuery = entityManager.createQuery("select o from MockVariable o where o.mockNotebookVersion.id = :editableId and o.cellId = :cellId and o.name = :name", MockVariable.class);
        variableQuery.setParameter("editableId", editableId);
        variableQuery.setParameter("cellId", cellId);
        variableQuery.setParameter("name", name);
        return variableQuery.getResultList().isEmpty() ? null : variableQuery.getResultList().get(0);
    }


}
