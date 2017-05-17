package portal.notebook.webapp.results;

import org.apache.wicket.MarkupContainer;

import java.io.Serializable;

/**
 * Created by timbo on 17/05/17.
 */
public abstract class ResultsPanelProvider implements Serializable {

    private final String id;
    private final String name;

    public ResultsPanelProvider(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract MarkupContainer createPanel(int tabIndex, Class dataType);
}
