package portal.webapp.notebook.persistence;

import toolkit.services.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;


@Entity
public class Notebook extends AbstractEntity {
    private String name;
    private byte[] data;

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(nullable = false)
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
