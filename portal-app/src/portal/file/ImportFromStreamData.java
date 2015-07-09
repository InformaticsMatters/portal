package portal.file;

import java.io.InputStream;
import java.util.Map;

/**
 * @author simetrias
 */
public class ImportFromStreamData {

    private String description;
    private InputStream inputStream;
    private Map<String, Class> fieldConfigMap;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Map<String, Class> getFieldConfigMap() {
        return fieldConfigMap;
    }

    public void setFieldConfigMap(Map<String, Class> fieldConfigMap) {
        this.fieldConfigMap = fieldConfigMap;
    }
}
