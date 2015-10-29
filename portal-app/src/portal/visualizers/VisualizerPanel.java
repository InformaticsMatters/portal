package portal.visualizers;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class VisualizerPanel extends Panel {

    private final VisualizerDescriptor visualizerDescriptor;

    public VisualizerPanel(String id, VisualizerDescriptor visualizerDescriptor) {
        super(id);
        setOutputMarkupId(true);
        this.visualizerDescriptor = visualizerDescriptor;
        addComponents();
    }

    private void addComponents() {
        add(new Label("description", visualizerDescriptor.getDescription()));
    }

}
