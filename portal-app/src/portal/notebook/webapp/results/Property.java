package portal.notebook.webapp.results;

import java.io.Serializable;

/**
 * Created by timbo on 30/08/16.
 */
class Property implements Serializable {
    String key;
    Object value;

    Property() {
    }

    Property(String key, Object value) {
        this.key = key;
        this.value = value;
    }

}
