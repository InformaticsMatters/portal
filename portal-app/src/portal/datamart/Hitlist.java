package portal.datamart;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author simetrias
 */
public class Hitlist implements Serializable {

    private Long id;
    private String status;
    private Date created;
    private String name;
    private String resource;
    private String owner;
    private Long[] items;
    private int size;

    public Long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    @JsonProperty("created")
    public void setCreated(Date created) {
        this.created = created;
    }

    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getResource() {
        return resource;
    }

    @JsonProperty("resource")
    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(int size) {
        this.size = size;
    }

    public Long[] getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(Long[] items) {
        this.items = items;
    }
}


