package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;

import java.util.List;
import java.util.Map;

/**
 * Created by timbo on 30/08/16.
 */
public class DatasetResultsPanel extends Panel {

    private CompoundPropertyModel<DatasetMetadata> datasetMetadataModel;
    private CompoundPropertyModel<List<? extends BasicObject>> resultsModel;

    public DatasetResultsPanel(String id, CompoundPropertyModel<DatasetMetadata> datasetMetadataModel, CompoundPropertyModel<List<? extends BasicObject>> resultsModel) {
        super(id);
        this.datasetMetadataModel = datasetMetadataModel;
        this.resultsModel = resultsModel;
        addComponents();
    }

    private void addComponents() {

        add(new ListView<BasicObject>("card", resultsModel) {

            @Override
            protected void populateItem(ListItem<BasicObject> listItem) {
                BasicObject o = listItem.getModelObject();
                Map<String,Class> mappings = datasetMetadataModel.getObject().getValueClassMappings();
                if (o instanceof MoleculeObject) {
                    listItem.add(new MoleculeObjectCardPanel("column", mappings, (MoleculeObject)o));
                } else {
                    listItem.add(new BasicObjectCardPanel("column", mappings, o));
                }
            }
        });

    }

}
