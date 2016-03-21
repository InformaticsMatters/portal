package portal.notebook.service;

import toolkit.services.AbstractEntity;

import javax.persistence.Entity;

@Entity
public class MockNotebookEditable extends AbstractEntity {
    private Long notebookId;
    private String userName;
    private byte[] json;

    public Long getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(Long notebookId) {
        this.notebookId = notebookId;
    }

    public byte[] getJson() {
        return json;
    }

    public void setJson(byte[] json) {
        this.json = json;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
