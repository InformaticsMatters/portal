package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.basic.Label;
import toolkit.wicket.semantic.SemanticModalPanel;

/**
 * Created by timbo on 27/08/2016.
 */
public class DatasetDetailsPanel extends SemanticModalPanel {

    public DatasetDetailsPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addContent();
    }

    private void addContent() {

//        Label titleLabel = new Label("title", "Cell ID here");
//        add(titleLabel);
    }
}
