package portal.notebook.service;

import toolkit.services.AbstractEntity;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;


@Entity
@Cacheable(value = false)
public class Notebook extends AbstractEntity {
    private String name;
    private String description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
