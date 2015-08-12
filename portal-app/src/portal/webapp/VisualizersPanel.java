package portal.webapp;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.ArrayList;

/**
 * @author simetrias
 */
public class VisualizersPanel extends Panel {

    private WebMarkupContainer visualizersContainer;

    private ListView<VisualizerDescriptor> listView;

    public VisualizersPanel(String id) {
        super(id);
        addVisualizers();
    }

    private void addVisualizers() {
        visualizersContainer = new WebMarkupContainer("visualizersContainer");
        visualizersContainer.setOutputMarkupId(true);

        listView = new ListView<VisualizerDescriptor>("descriptors", new ArrayList<>()) {

            @Override
            protected void populateItem(ListItem<VisualizerDescriptor> listItem) {
                VisualizerDescriptor visualizerDescriptor = listItem.getModelObject();
                listItem.add(new VisualizerPanel("visualizer"));
                listItem.setOutputMarkupId(true);

            }
        };
        visualizersContainer.add(listView);

        add(visualizersContainer);
    }



}
