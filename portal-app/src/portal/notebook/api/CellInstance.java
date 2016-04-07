package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlRootElement
public class CellInstance implements Serializable {
    private final static long serialVersionUID = 1l;

    private CellDefinition cellDefinition;
    private final Map<String, BindingInstance> bindingInstanceMap = new LinkedHashMap<>();
    private final Map<String, VariableInstance> variableInstanceMap = new LinkedHashMap<>();
    private final Map<String, OptionInstance> optionInstanceMap = new LinkedHashMap<>();
    private Long id;
    private String name;
    private int positionLeft;
    private int positionTop;
    private Integer sizeWidth;
    private Integer sizeHeight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CellDefinition getCellDefinition() {
        return cellDefinition;
    }

    public void setCellDefinition(CellDefinition cellDefinition) {
        this.cellDefinition = cellDefinition;
    }

    public Map<String, BindingInstance> getBindingInstanceMap() {
        return bindingInstanceMap;
    }

    public Map<String, VariableInstance> getVariableInstanceMap() {
        return variableInstanceMap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, OptionInstance> getOptionInstanceMap() {
        return optionInstanceMap;
    }

    public int getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(int positionLeft) {
        this.positionLeft = positionLeft;
    }

    public int getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(int positionTop) {
        this.positionTop = positionTop;
    }

    public Integer getSizeWidth() {
        return sizeWidth;
    }

    public void setSizeWidth(Integer sizeWidth) {
        this.sizeWidth = sizeWidth;
    }

    public Integer getSizeHeight() {
        return sizeHeight;
    }

    public void setSizeHeight(Integer sizeHeight) {
        this.sizeHeight = sizeHeight;
    }

}
