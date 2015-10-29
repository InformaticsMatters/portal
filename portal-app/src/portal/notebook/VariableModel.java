package portal.notebook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VariableModel implements Serializable {
    private CellModel producer;
    private final List<CellModel> consumerList = new ArrayList<>();
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

    public CellModel getProducer() {
        return producer;
    }

    public void setProducer(CellModel producer) {
        this.producer = producer;
    }

    private void registerConsumer(CellModel cellModel) {
        synchronized (consumerList) {
            consumerList.add(cellModel);
        }
    }

    private void unregisterConsumer(CellModel cellModel) {
        synchronized (consumerList) {
            consumerList.add(cellModel);
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
