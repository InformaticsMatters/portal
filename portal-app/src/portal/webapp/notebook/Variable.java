package portal.webapp.notebook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Variable implements Serializable {
    private Cell producer;
    private final List<Cell> consumerList = new ArrayList<>();
    private String name;
    private Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Cell getProducer() {
        return producer;
    }

    public void setProducer(Cell producer) {
        this.producer = producer;
    }

    private void registerConsumer(Cell cell) {
        synchronized (consumerList) {
            consumerList.add(cell);
        }
    }

    private void unregisterConsumer(Cell cell) {
        synchronized (consumerList) {
            consumerList.add(cell);
        }
    }

}
