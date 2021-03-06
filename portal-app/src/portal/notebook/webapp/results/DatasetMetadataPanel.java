package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by timbo on 27/08/2016.
 */
public class DatasetMetadataPanel extends Panel {

    private IModel<DatasetMetadata> datasetMetadataModel;

    public DatasetMetadataPanel(String id, IModel<DatasetMetadata> datasetMetadataModel) {
        super(id);
        this.datasetMetadataModel = datasetMetadataModel;
        addContent();
    }

    private void addContent() {
        Form<DatasetMetadata> form = new Form<>("meta");
        add(form);
        form.setModel(datasetMetadataModel);

        form.add(new ListView<Property>("properties", new MetadataPropertiesModel()) {

            @Override
            protected void populateItem(ListItem<Property> item) {
                Property p = item.getModelObject();
                item.add(new Label("key", p.key));
                Object value = p.value;
                item.add(ResultsUtils.generateContent("value", value));

            }
        });

        form.add(new ListView<Field>("fields", new FieldPropertiesModel()) {

            @Override
            protected void populateItem(ListItem<Field> item) {
                Field f = item.getModelObject();
                item.add(new Label("fieldName", f.name));
                String propCount = f.properties.size() + (f.properties.size() == 1 ? " property" : " properties");
                item.add(new Label("fieldDesc", "[" + f.type + ", " + propCount + "]"));
                item.add(new ListView<Property>("field", f.properties) {

                    @Override
                    protected void populateItem(ListItem<Property> item) {
                        Property p = item.getModelObject();
                        item.add(new Label("fieldkey", p.key));
                        Object value = p.value;
                        item.add(ResultsUtils.generateContent("fieldvalue", value));
                    }
                });
            }
        });
    }

    class MetadataPropertiesModel extends LoadableDetachableModel<List<Property>> {

        @Override
        protected List<Property> load() {
            DatasetMetadata<? extends BasicObject> meta = datasetMetadataModel.getObject();
            if (meta == null) {
                return Collections.emptyList();
            }
            List<Property> results = new ArrayList<>();
            results.add(new Property("size", meta.getSize()));
            results.add(new Property("type", meta.getType().getSimpleName()));
            Map<String, Object> props = meta.getProperties();
            for (Map.Entry<String, Object> e : props.entrySet()) {
                results.add(new Property(e.getKey(), e.getValue()));
            }
            return results;
        }

    }

    class FieldPropertiesModel extends LoadableDetachableModel<List<Field>> {

        @Override
        protected List<Field> load() {
            DatasetMetadata meta = datasetMetadataModel.getObject();
            if (meta == null) {
                return Collections.emptyList();
            }
            List<Field> results = new ArrayList<>();
            Map<String, DatasetMetadata.PropertiesHolder> phs = meta.getFieldMetaPropsMap();
            Map<String, Class> types = meta.getValueClassMappings();
            for (Map.Entry<String, Class> ftypes : types.entrySet()) {
                Field f = new Field();
                f.name = ftypes.getKey();
                f.type = ftypes.getValue().getSimpleName();
                DatasetMetadata.PropertiesHolder ph = phs.get(f.name);
                List<Property> props = new ArrayList<>();
                if (ph != null) {
                    Map<String, DatasetMetadata.PropertiesHolder> fldProps = meta.getFieldMetaPropsMap();
                    Map<String, Object> map = fldProps.get(f.name).getValues();
                    for (Map.Entry<String, Object> e : map.entrySet()) {
                        props.add(new Property(e.getKey(), e.getValue()));
                    }
                }
                f.properties = props;
                results.add(f);
            }

            return results;
        }
    }

    class Field implements Serializable {
        String name;
        String type;
        List<Property> properties;
    }

}
