package portal.notebook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Variable implements Serializable {
    private Cell producer;
    private final List<Cell> consumerList = new ArrayList<>();
    private String name;
    private Object value;
    private transient List<VariableChangeListener> changeListenerList;

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
        if ((value != null && !value.equals(this.value)) || (this.value != null && !this.value.equals(value))) {
            Object oldValue = this.value;
            this.value = value;
            notifyValueChanged(oldValue);
        }
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

    public synchronized void addChangeListener(VariableChangeListener changeListener) {
        if (changeListenerList == null) {
            changeListenerList = new ArrayList<>();
        }
        changeListenerList.add(changeListener);
    }

    public synchronized  void removeChangeListener(VariableChangeListener changeListener) {
        if (changeListenerList != null) {
            changeListenerList.remove(changeListener);
        }
    }

    private synchronized void notifyValueChanged(Object oldValue) {
        if (changeListenerList != null) {
            for (VariableChangeListener listener : changeListenerList) {
                listener.onValueChanged(this, oldValue);
            }
        }
    }

    public void notifyRemoved() {
        if (changeListenerList != null) {
            for (VariableChangeListener listener : changeListenerList) {
                listener.onVariableRemoved(this);
            }
        }
    }

    public String toString() {
        return (producer == null ? "" : producer.getName())
                + "." + (name == null ? "" : name);
    }

}
