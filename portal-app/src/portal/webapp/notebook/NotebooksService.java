package portal.webapp.notebook;

import portal.webapp.notebook.persistence.Notebook;
import toolkit.services.PU;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class NotebooksService {
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

    public NotebookContents retrieveNotebookContents(Long id) {
        try {
            Notebook notebook = entityManager.find(Notebook.class, id);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(notebook.getData());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (NotebookContents)objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void storeNotebook(StoreNotebookData storeNotebookData) {
        try {
            boolean insert = storeNotebookData.getNotebookInfo().getId() == null;
            Notebook notebook = insert ? new Notebook() : entityManager.find(Notebook.class, storeNotebookData.getNotebookInfo().getId());
            notebook.setName(storeNotebookData.getNotebookInfo().getName());
            if (insert) {
                entityManager.persist(notebook);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(storeNotebookData.getNotebookContents());
            objectOutputStream.flush();
            byteArrayOutputStream.flush();
            notebook.setData(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
