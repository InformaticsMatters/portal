package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;

import java.io.Serializable;
import java.util.ArrayList;
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



        add(new ListView<Datum>("structureCard", new ResultsListModel()) {

            @Override
            protected void populateItem(ListItem<Datum> listItem) {
                Datum d = listItem.getModelObject();

                listItem.add(new Label("uuid", d.UUID));

                listItem.add(new ListView<Property>("fields", new ObjectPropertiesModel(d)) {

                    @Override
                    protected void populateItem(ListItem<Property> listItem) {
                        Property p = listItem.getModelObject();
                        listItem.add(new Label("fieldkey", p.key));
                        listItem.add(new MultiLineLabel("fieldvalue", p.value == null ? "" : p.value.toString()));
                    }
                });
            }
        });
    }

    class ObjectPropertiesModel extends LoadableDetachableModel<List<Property>> {
        Datum d;
        ObjectPropertiesModel(Datum d) {
            this.d = d;
        }

        @Override
        protected List<Property> load() {
            List<Property> list = new ArrayList<>();
            d.fieldTypes.forEach((k,v) -> {
                list.add(new Property(k, d.object.getValue(k)));
            });
            return list;
        }
    }


    class ResultsListModel extends LoadableDetachableModel<List<Datum>> {

        @Override
        protected List<Datum> load() {
            Map<String,Class> fieldTypes = datasetMetadataModel.getObject().getValueClassMappings();
            List<Datum> data = new ArrayList<>();
            List<? extends BasicObject> results = resultsModel.getObject();
            results.stream().forEach((o) -> {
                Datum d = new Datum();
                d.UUID = o.getUUID().toString();
                d.fieldTypes = fieldTypes;
                d.object = o;
                data.add(d);
            });
            return data;
        }
    }

    class Datum implements Serializable {
        String UUID;
        Map<String,Class> fieldTypes;
        BasicObject object;
    }
}
