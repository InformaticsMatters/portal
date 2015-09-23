package portal.webapp.notebook;

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
        if ((value == null && this.value != null) || !value.equals(this.value)) {
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

    public synchronized void registerChangeListener(VariableChangeListener changeListener) {
        if (changeListenerList == null) {
            changeListenerList = new ArrayList<>();
        }
        changeListenerList.add(changeListener);
    }

    private synchronized void notifyValueChanged(Object oldValue) {
        if (changeListenerList != null) {
            for (VariableChangeListener listener : changeListenerList) {
                listener.onValueChanged(this, oldValue);
            }
        }
    }

}
