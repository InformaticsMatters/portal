package portal.notebook.service;

import portal.notebook.execution.api.VariableType;

import java.io.Serializable;

public class Variable implements Serializable {
    private Long id;
    private Cell producerCell;
    private String name;
    private VariableType variableType;
    private Object value;

    public Cell getProducerCell() {
        return producerCell;
    }

    public void setProducerCell(Cell producerCell) {
        this.producerCell = producerCell;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public void setVariableType(VariableType variableType) {
        this.variableType = variableType;
    }
}
