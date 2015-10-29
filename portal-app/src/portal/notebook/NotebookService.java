package portal.notebook;

import toolkit.services.PU;
import toolkit.services.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
@Transactional
public class NotebookService {
    @Inject
    @PU(puName = NotebookConstants.PU_NAME)
    private EntityManager entityManager;


    public List<NotebookInfo> listNotebookInfo() {
        List<NotebookInfo> list = new ArrayList<>();
        for (Notebook notebook : entityManager.createQuery("select o from Notebook o order by o.name", Notebook.class).getResultList()) {
            NotebookInfo notebookInfo = new NotebookInfo();
            notebookInfo.setId(notebook.getId());
            notebookInfo.setName(notebook.getName());
            list.add(notebookInfo);
        }
        return list;
    }

    public NotebookInfo retrieveNotebookInfo(Long id) {
        Notebook notebook = entityManager.find(Notebook.class, id);
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setId(notebook.getId());
        notebookInfo.setName(notebook.getName());
        return notebookInfo;
    }

    public NotebookModel retrieveNotebookContents(Long id) {
        try {
            Notebook notebook = entityManager.find(Notebook.class, id);
            return NotebookModel.fromBytes(notebook.getData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long storeNotebook(StoreNotebookData storeNotebookData) {
        try {
            boolean insert = storeNotebookData.getNotebookInfo().getId() == null;
            Notebook notebook = insert ? new Notebook() : entityManager.find(Notebook.class, storeNotebookData.getNotebookInfo().getId());
            notebook.setName(storeNotebookData.getNotebookInfo().getName());
            if (insert) {
                entityManager.persist(notebook);
            }
            notebook.setData(storeNotebookData.getNotebookModel().toBytes());
            NotebookHistory notebookHistory = new NotebookHistory();
            notebookHistory.setNotebook(notebook);
            notebookHistory.setData(notebook.getData());
            notebookHistory.setRevisionDate(new Date());
            notebookHistory.setRevisionTime(new Date());
            return notebook.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
